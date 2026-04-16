package com.masterapp.queueeaseapp.utils

import kotlin.math.max

data class QueueSnapshot(
    val timestampMs: Long,
    val queueLength: Int,
    val currentServing: Int
)

data class SimulationScenario(
    val extraStaff: Int = 0,
    val speedMultiplier: Double = 1.0
)

data class QueueSimulationResult(
    val arrivalRatePerMinute: Double,
    val serviceRatePerMinute: Double,
    val effectiveServiceRatePerMinute: Double,
    val currentWaitMinutes: Int,
    val waitAfter15Minutes: Int,
    val waitAfter30Minutes: Int,
    val waitAfter60Minutes: Int,
    val futureQueue15: Int,
    val futureQueue30: Int,
    val futureQueue60: Int
)

object QueueSimulationEngine {
    private const val FALLBACK_SERVICE_RATE_PER_MINUTE = 0.5 // 1 user per 2 min

    fun simulate(
        history: List<QueueSnapshot>,
        currentQueueLength: Int,
        currentWaitMinutes: Int,
        scenario: SimulationScenario
    ): QueueSimulationResult {
        val rates = calculateRates(history)
        val baselineServiceRate = max(rates.serviceRatePerMinute, FALLBACK_SERVICE_RATE_PER_MINUTE)
        val effectiveServiceRate = max(
            0.1,
            (baselineServiceRate + scenario.extraStaff * baselineServiceRate) * scenario.speedMultiplier
        )

        val future15 = predictFutureQueue(currentQueueLength, rates.arrivalRatePerMinute, effectiveServiceRate, 15)
        val future30 = predictFutureQueue(currentQueueLength, rates.arrivalRatePerMinute, effectiveServiceRate, 30)
        val future60 = predictFutureQueue(currentQueueLength, rates.arrivalRatePerMinute, effectiveServiceRate, 60)

        return QueueSimulationResult(
            arrivalRatePerMinute = rates.arrivalRatePerMinute,
            serviceRatePerMinute = rates.serviceRatePerMinute,
            effectiveServiceRatePerMinute = effectiveServiceRate,
            currentWaitMinutes = currentWaitMinutes,
            waitAfter15Minutes = predictWaitMinutes(future15, effectiveServiceRate),
            waitAfter30Minutes = predictWaitMinutes(future30, effectiveServiceRate),
            waitAfter60Minutes = predictWaitMinutes(future60, effectiveServiceRate),
            futureQueue15 = future15,
            futureQueue30 = future30,
            futureQueue60 = future60
        )
    }

    fun predictFutureQueue(
        currentQueueLength: Int,
        arrivalRatePerMinute: Double,
        serviceRatePerMinute: Double,
        timeMinutes: Int
    ): Int {
        val projected = currentQueueLength + (arrivalRatePerMinute - serviceRatePerMinute) * timeMinutes
        return max(0, projected.toInt())
    }

    private fun predictWaitMinutes(projectedQueueLength: Int, serviceRatePerMinute: Double): Int {
        if (projectedQueueLength <= 0) return 0
        return max(1, (projectedQueueLength / max(serviceRatePerMinute, 0.1)).toInt())
    }

    private fun calculateRates(history: List<QueueSnapshot>): Rates {
        if (history.size < 2) {
            return Rates(arrivalRatePerMinute = 0.0, serviceRatePerMinute = FALLBACK_SERVICE_RATE_PER_MINUTE)
        }

        val first = history.first()
        val last = history.last()
        val durationMinutes = ((last.timestampMs - first.timestampMs).toDouble() / 60_000.0).coerceAtLeast(0.1)

        val queueDelta = last.queueLength - first.queueLength
        val servedDelta = max(0, last.currentServing - first.currentServing)

        // queueDelta = arrivals - served  => arrivals = queueDelta + served
        val arrivals = max(0.0, queueDelta + servedDelta.toDouble())
        val arrivalRate = arrivals / durationMinutes
        val serviceRate = max(0.0, servedDelta / durationMinutes)

        return Rates(arrivalRatePerMinute = arrivalRate, serviceRatePerMinute = serviceRate)
    }

    private data class Rates(
        val arrivalRatePerMinute: Double,
        val serviceRatePerMinute: Double
    )
}
