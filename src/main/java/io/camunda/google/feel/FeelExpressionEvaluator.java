package io.camunda.google.feel;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;

import io.camunda.google.config.ThymeleafConfig;
import scala.util.Either;

public class FeelExpressionEvaluator implements IStandardVariableExpressionEvaluator {

    private FeelEngine feelEngine = null;
    DateTimeFormatter datetimeFormatter = null; 
    DateTimeFormatter dateFormatter = null; 
    private String datetimePattern = "dd/MM/yyyy - HH:mm:ss";
    private String datePattern = "dd/MM/yyy";
    
    public FeelExpressionEvaluator() {
        super();
    }
    public FeelExpressionEvaluator(ThymeleafConfig config) {
        super();
        datePattern = config.getDatePattern();
        datetimePattern = config.getDateTimePattern();
    }
    
    private FeelEngine getFeelEngine() {
        if (feelEngine == null) {
            feelEngine = new FeelEngine.Builder().valueMapper(SpiServiceLoader.loadValueMapper()).functionProvider(SpiServiceLoader.loadFunctionProvider())
                    .build();
        }
        return feelEngine;
    }
    
    public DateTimeFormatter getDateTimeFormatter() {
        if (dateFormatter == null) {
            dateFormatter = DateTimeFormatter.ofPattern(datetimePattern);
        }
        return dateFormatter;
    }
    public DateTimeFormatter getDateFormatter() {
        if (datetimeFormatter == null) {
            datetimeFormatter = DateTimeFormatter.ofPattern(datePattern);
        }
        return datetimeFormatter;
    }

    @Override
    public Object evaluate(IExpressionContext context, IStandardVariableExpression expression, StandardExpressionExecutionContext expContext) {
        final Map<String, Object> variables = new HashMap<>();
        for (String name : context.getVariableNames()) {
            variables.put(name, context.getVariable(name));
        }
        final Either<FeelEngine.Failure, Object> result = getFeelEngine().evalExpression(expression.getExpression(), variables);
        if (result.isRight()) {
            Object value = result.right().get();
            if (value instanceof ZonedDateTime) {
                return ((ZonedDateTime)value).format(getDateTimeFormatter());
            }
            if (value instanceof LocalDate) {
                return ((LocalDate)value).format(getDateFormatter());
            }
            return value;
        } else {
            final FeelEngine.Failure failure = result.left().get();
            throw new RuntimeException(failure.message());
        }
    }
}
