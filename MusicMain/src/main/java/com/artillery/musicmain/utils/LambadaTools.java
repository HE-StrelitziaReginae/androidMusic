package com.artillery.musicmain.utils;

import java.util.function.Consumer;

import io.reactivex.functions.BiConsumer;

/**
 * @author ArtilleryOrchid
 */
public class LambadaTools {
    public static <T> Consumer<? super T> forEachWithIndex(BiConsumer<T, Integer> biConsumer) {
        class IncrementInt {
            int i = 0;

            public int getAndIncrement() {
                return i++;
            }
        }
        IncrementInt incrementInt = new IncrementInt();
        return t -> {
            try {
                biConsumer.accept(t, incrementInt.getAndIncrement());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
