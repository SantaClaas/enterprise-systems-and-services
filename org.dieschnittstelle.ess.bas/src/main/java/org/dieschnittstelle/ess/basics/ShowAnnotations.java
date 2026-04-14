package org.dieschnittstelle.ess.basics;


import org.dieschnittstelle.ess.basics.annotations.AnnotatedStockItemBuilder;
import org.dieschnittstelle.ess.basics.annotations.DisplayAs;
import org.dieschnittstelle.ess.basics.annotations.StockItemProxyImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.dieschnittstelle.ess.basics.reflection.ReflectedStockItemBuilder.getAccessorNameForField;
import static org.dieschnittstelle.ess.utils.Utils.*;

public class ShowAnnotations {

    public static void main(String[] args) {
        // we initialise the collection
        StockItemCollection collection = new StockItemCollection(
                "stockitems_annotations.xml", new AnnotatedStockItemBuilder());
        // we load the contents into the collection
        collection.load();

        for (IStockItem consumable : collection.getStockItems()) {
            showAttributes(((StockItemProxyImpl) consumable).getProxiedObject());
        }

        // we initialise a consumer
        Consumer consumer = new Consumer();
        // ... and let them consume
        consumer.doShopping(collection.getStockItems());
    }


    private static void runBas2(Object instance) {
        var class_ = instance.getClass();

        var fields = class_.getDeclaredFields();
        // Easiest tuple like value I could find is Map.Entry
        var values = new ArrayList<Map.Entry<String, String>>();
        for (var field : fields) {
            var fieldName = field.getName();
            var accessorName = getAccessorNameForField("get", fieldName);
            Method accessor;
            // Prefer to handle each exception individually as that allows to easier pinpoint errors
//			TODO handle accessor not existing by either skipping field or accessing it directly
            try {
                accessor = class_.getMethod(accessorName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            Object fieldValue;
            try {
                fieldValue = accessor.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            values.add(new AbstractMap.SimpleEntry<>(fieldName, fieldValue.toString()));
        }


        var className = class_.getSimpleName();

        var valuesList = values.stream().map(pair -> pair.getKey() + ":" + pair.getValue()).collect(Collectors.joining(", "));
        System.out.printf("{%s %s}%n", className, valuesList);
    }


    private static void runBas3(Object instance) {
        var class_ = instance.getClass();

        var fields = class_.getDeclaredFields();
        // Easiest tuple like value I could find is Map.Entry
        var values = new ArrayList<Map.Entry<String, String>>();
        for (var field : fields) {
            var fieldName = field.getName();
            var accessorName = getAccessorNameForField("get", fieldName);
            Method accessor;
            // Prefer to handle each exception individually as that allows to easier pinpoint errors
//			TODO handle accessor not existing by either skipping field or accessing it directly
            try {
                accessor = class_.getMethod(accessorName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // Field annotation takes precedence over get accessor annotation
            if (field.isAnnotationPresent(DisplayAs.class)) {
                var annotation = field.getAnnotation(DisplayAs.class);
                fieldName = annotation.value();
            } else if (accessor.isAnnotationPresent(DisplayAs.class)) {
                var annotation = accessor.getAnnotation(DisplayAs.class);
                fieldName = annotation.value();
            }

            Object fieldValue;
            try {
                fieldValue = accessor.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            values.add(new AbstractMap.SimpleEntry<>(fieldName, fieldValue.toString()));
        }


        var className = class_.getSimpleName();

        var valuesList = values.stream().map(pair -> pair.getKey() + ":" + pair.getValue()).collect(Collectors.joining(", "));
        System.out.printf("{%s %s}%n", className, valuesList);
    }

    private static void showAttributes(Object instance) {
        show("class is: " + instance.getClass());


        try {
            runBas2(instance);

            runBas3(instance);
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("showAnnotations(): exception occurred: " + error, error);
        }

    }

}
