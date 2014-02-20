package com.fererlab.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * acm
 */
@XStreamAlias("content")
@XStreamConverter(PairConverter.class)
public class Content<Key, Value> extends Pair<Key, Value> {

    public Content<Key, Value> add(Key key, Value value) {
        return (Content<Key, Value>) super.add(key, value);
    }

}