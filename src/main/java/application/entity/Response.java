package application.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaoxi666
 * @date 2022-08-18 20:50
 */
@Data
public class Response<T> implements Serializable {

    private int code;

    private String msg;

    private T data;
}