package com.fererlab.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * acm
 */
@XStreamAlias("header")
@XStreamConverter(PairConverter.class)
public class Header<Key, Value> extends Pair<Key, Value> {

    public static final String STATUS = "success";
    public static final String MESSAGE = "";

    public Header<Key, Value> add(Key key, Value value) {
        return (Header<Key, Value>) super.add(key, value);
    }

}