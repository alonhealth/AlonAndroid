package com.alonhealth.android.models

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Base class for all health data models
 */
sealed class HealthData {
    abstract val startTime: Instant
    abstract val endTime: Instant
    abstract val source: String
}

/**
 * Model class for heart rate data
 */
data class HeartRateData(
    override val startTime: Instant,
    override val endTime: Instant,
    override val source: String,
    val samples: List<HeartRateSample>
) : HealthData()

/**
 * Model class for a heart rate sample
 */
data class HeartRateSample(
    val time: Instant,
    val beatsPerMinute: Long
)

/**
 * Model class for heart rate variability data
 */
data class HeartRateVariabilityData(
    override val startTime: Instant,
    override val endTime: Instant,
    override val source: String,
    val heartRateVariabilityMillis: Long
) : HealthData()

/**
 * Model class for step count data
 */
data class StepCountData(
    override val startTime: Instant,
    override val endTime: Instant,
    override val source: String,
    val count: Long
) : HealthData()

/**
 * Model class for sleep data
 */
data class SleepData(
    override val startTime: Instant,
    override val endTime: Instant,
    override val source: String,
    val stages: List<SleepStage>
) : HealthData()

/**
 * Model class for a sleep stage
 */
data class SleepStage(
    val startTime: Instant,
    val endTime: Instant,
    val stage: SleepStageType
)

/**
 * Enum for sleep stage types
 */
enum class SleepStageType {
    UNKNOWN,
    AWAKE,
    SLEEPING,
    OUT_OF_BED,
    LIGHT,
    DEEP,
    REM
}

/**
 * Model class for weight data
 */
data class WeightData(
    override val startTime: Instant,
    override val endTime: Instant,
    override val source: String,
    val weightKg: Double
) : HealthData()

/**
 * Extension function to convert HeartRateRecord to HeartRateData
 */
fun HeartRateRecord.toHeartRateData(): HeartRateData {
    return HeartRateData(
        startTime = this.startTime,
        endTime = this.endTime,
        source = this.metadata.dataOrigin.packageName,
        samples = this.samples.map {
            HeartRateSample(
                time = it.time,
                beatsPerMinute = it.beatsPerMinute
            )
        }
    )
}

/**
 * Extension function to convert HeartRateVariabilityRmssdRecord to HeartRateVariabilityData
 */
fun HeartRateVariabilityRmssdRecord.toHeartRateVariabilityData(): HeartRateVariabilityData {
    return HeartRateVariabilityData(
        startTime = this.time,
        endTime = this.time,
        source = this.metadata.dataOrigin.packageName,
        heartRateVariabilityMillis = this.heartRateVariabilityMillis
    )
}

/**
 * Extension function to convert StepsRecord to StepCountData
 */
fun StepsRecord.toStepCountData(): StepCountData {
    return StepCountData(
        startTime = this.startTime,
        endTime = this.endTime,
        source = this.metadata.dataOrigin.packageName,
        count = this.count
    )
}

/**
 * Extension function to convert SleepSessionRecord to SleepData
 */
fun SleepSessionRecord.toSleepData(): SleepData {
    return SleepData(
        startTime = this.startTime,
        endTime = this.endTime,
        source = this.metadata.dataOrigin.packageName,
        stages = this.stages.map {
            SleepStage(
                startTime = it.startTime,
                endTime = it.endTime,
                stage = when (it.stage) {
                    SleepSessionRecord.STAGE_TYPE_UNKNOWN -> SleepStageType.UNKNOWN
                    SleepSessionRecord.STAGE_TYPE_AWAKE -> SleepStageType.AWAKE
                    SleepSessionRecord.STAGE_TYPE_SLEEPING -> SleepStageType.SLEEPING
                    SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> SleepStageType.OUT_OF_BED
                    SleepSessionRecord.STAGE_TYPE_LIGHT -> SleepStageType.LIGHT
                    SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStageType.DEEP
                    SleepSessionRecord.STAGE_TYPE_REM -> SleepStageType.REM
                    else -> SleepStageType.UNKNOWN
                }
            )
        }
    )
}

/**
 * Extension function to convert WeightRecord to WeightData
 */
fun WeightRecord.toWeightData(): WeightData {
    return WeightData(
        startTime = this.time,
        endTime = this.time,
        source = this.metadata.dataOrigin.packageName,
        weightKg = this.weight.inKilograms
    )
} 