package com.youyou.uuelectric.renter.Utils.eventbus;

/**
 * User: qing
 * Date: 2015/9/1
 * Desc: EventBus分发的事件的基类，所有事件都继承自该事件
 */
public class BaseEvent {

    /**
     * 事件类型
     */
    private String type;
    /**
     * 事件处理携带的额外数据
     */
    private Object ExtraData;

    public BaseEvent(String type) {
        this.type = type;
    }

    public BaseEvent(String type, Object ExtraData) {
        this.type = type;
        this.ExtraData = ExtraData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getExtraData() {
        return ExtraData;
    }

    public void setExtraData(Object extraData) {
        ExtraData = extraData;
    }
}
