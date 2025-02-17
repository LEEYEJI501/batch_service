package com.example.common;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import com.example.common.TechnicalIndicator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Embeddable
@Data
public class MACD implements TechnicalIndicator {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "macd_line", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    @BatchSize(size = 100)
    private List<Double> macdLine;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "signal_line", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    @BatchSize(size = 100)
    private List<Double> signalLine;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "histogram", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    @BatchSize(size = 100)
    private List<Double> histogram;

    @Override
    public void calculate(List<Double> prices) {
        List<Double> ema12 = calculateEMA(prices, 12);
        List<Double> ema26 = calculateEMA(prices, 26);
        calculateMACD(ema12, ema26);
    }

    private void calculateMACD(List<Double> ema12, List<Double> ema26) {
        macdLine = new ArrayList<>(ema12.size());
        for (int i = 0; i < ema12.size(); i++) {
            macdLine.add(ema12.get(i) - ema26.get(i));
        }
        signalLine = calculateEMA(macdLine, 9);
        histogram = new ArrayList<>(macdLine.size());
        for (int i = 0; i < macdLine.size(); i++) {
            histogram.add(macdLine.get(i) - signalLine.get(i));
        }
    }

    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>(prices.size());
        double multiplier = 2.0 / (period + 1);
        ema.add(prices.getFirst());
        for (int i = 1; i < prices.size(); i++) {
            double value = (prices.get(i) - ema.get(i - 1)) * multiplier + ema.get(i - 1);
            ema.add(value);
        }
        return ema;
    }

    @Override
    public Map<String, List<Double>> getResults() {
        Map<String, List<Double>> results = new HashMap<>();
        results.put("MACDLine", macdLine);
        results.put("SignalLine", signalLine);
        results.put("Histogram", histogram);
        return results;
    }
}
