package com.motosmart.launcher.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

data class CompassState(
    val isAvailable: Boolean = false,
    val isActive: Boolean = false,
    val headingDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE
)

/**
 * Uses the rotation vector sensor when present, with an accelerometer + magnetometer
 * fallback. A missing compass is reported rather than fabricated.
 */
class CompassRepository(private val context: Context) {

    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val _state = MutableStateFlow(CompassState(isAvailable = hasCompass()))
    val state: StateFlow<CompassState> = _state.asStateFlow()

    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var listening = false

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var lastHeading = 0f

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    updateHeading(event.accuracy)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    lowPass(event.values, gravity)
                    hasGravity = true
                    updateFromVectors(event.accuracy)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    lowPass(event.values, geomagnetic)
                    hasGeomagnetic = true
                    updateFromVectors(event.accuracy)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                _state.update { it.copy(accuracy = accuracy) }
            }
        }
    }

    fun hasCompass(): Boolean {
        val manager = sensorManager ?: return false
        return manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
            (manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)
    }

    fun start() {
        if (listening) return
        if (!hasCompass()) {
            _state.update { it.copy(isAvailable = false, isActive = false) }
            return
        }
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(
                listener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            accelerometer?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
            magnetometer?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        listening = true
        _state.update { it.copy(isAvailable = true, isActive = true) }
    }

    fun stop() {
        if (!listening) return
        runCatching { sensorManager.unregisterListener(listener) }
        listening = false
        hasGravity = false
        hasGeomagnetic = false
        _state.update { it.copy(isActive = false) }
    }

    private fun updateFromVectors(accuracy: Int) {
        if (!hasGravity || !hasGeomagnetic) return
        if (!SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) return
        updateHeading(accuracy)
    }

    private fun updateHeading(accuracy: Int) {
        SensorManager.getOrientation(rotationMatrix, orientation)
        val degrees = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
        // Shortest-path smoothing avoids a jump between 359° and 0°.
        val delta = ((degrees - lastHeading + 540f) % 360f) - 180f
        val smoothed = (lastHeading + delta * SMOOTHING + 360f) % 360f
        lastHeading = if (abs(delta) > 45f) degrees else smoothed
        _state.update {
            it.copy(
                isAvailable = true,
                headingDegrees = lastHeading,
                accuracy = accuracy
            )
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        for (index in 0 until 3) {
            output[index] += (input[index] - output[index]) * 0.15f
        }
    }

    private companion object {
        const val SMOOTHING = 0.18f
    }
}
