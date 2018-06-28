package com.miracle.validate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Description:参数校验器
 * 在使用时除了设置校验条件,还必须设置校验成功执行函数,这个函数会返回值
 *
 * @author guobin On date 2018/6/28.
 * @version 1.0
 * @since jdk 1.8
 * @param <T> 被校验的参数类型
 * @param <U> 校验之后执行函数的返回结果
 */
public class AppliableValidator<T, U> extends AbstractValidator<T> {

    /**
     * 校验成功之后的调用函数
     */
    private Function<T, U> successHandler;

    private AppliableValidator(T value, String nullValueCode, boolean fastValidate) {
        super(value, nullValueCode, fastValidate);
    }

    /**
     * 返回一个带传入值的{@code AcceptableValidator}
     *
     * @param value 带校验的对象
     * @param <P> 对象的类型
     * @param <R> 返回结果的类型
     * @return 一个校验者
     */
    public static <P, R> AppliableValidator<P, R> of(P value) {
        return of(value, false);
    }

    /**
     * 返回一个带传入值的{@code AcceptableValidator}
     *
     * @param value 带校验的对象
     * @param fastValidate 是否是快速校验模式
     * @param <P> 对象的类型
     * @return 一个校验者
     */
    public static <P, R> AppliableValidator<P, R> of(P value, boolean fastValidate) {
        return of(value, NO_ERROR_CODE, fastValidate);
    }

    /**
     * 返回一个带传入值的{@code AcceptableValidator}
     *
     * @param value 带校验的对象
     * @param <P> 对象的类型
     * @param nullValueCode 当校验对象为空时返回的错误码
     * @param fastValidate 是否是快速校验模式
     * @return 一个校验者
     * @throws NullPointerException 当传入的空对象错误码为空时抛出异常
     */
    public static <P, R> AppliableValidator<P, R> of(P value, String nullValueCode, boolean fastValidate) {
        if (nullValueCode == null) {
            throw new NullPointerException(NULL_ERROR_CODE_MESSAGE);
        }
        return new AppliableValidator<>(value, nullValueCode, fastValidate);
    }

    /**
     * 判断mapper返回值非空
     * @param mapper 传入的mapper
     * @param errorMsg 错误信息
     * @param <R> mapper返回的类型
     * @return 返回更新后的校验者
     */
    public <R> AppliableValidator<T, U> notNull(Function<? super T, ? extends R> mapper, String errorMsg) {
        return this.notNull(mapper, errorMsg, NO_ERROR_CODE);
    }

    /**
     * 判断mapper返回值非空
     * @param mapper 传入的mapper
     * @param errorMsg 错误信息
     * @param errorCode 错误码
     * @param <R> mapper返回的类型
     * @return 返回更新后的校验者
     */
    public <R> AppliableValidator<T, U> notNull(Function<? super T, ? extends R> mapper,
                                                String errorMsg,
                                                String errorCode) {
        this.checkValue();
        if (this.keepValidating() && this.value != null && mapper.apply(this.value) == null) {
            this.setError(errorCode, errorMsg);
        }
        return this;
    }

    /**
     * 自定义校验方式
     * 当传入的{@code predicate}通过时视作校验成功
     * @param predicate 传入的断言
     * @param errorMsg 错误信息
     * @return 返回更新后的校验者
     */
    public AppliableValidator<T, U> on(Predicate<? super T> predicate, String errorMsg) {
        return this.on(predicate, errorMsg, NO_ERROR_CODE);
    }

    /**
     * 自定义校验方式
     * 当传入的{@code predicate}通过时视作校验成功
     * @param predicate 传入的断言
     * @param errorMsg 错误信息
     * @param errorCode 错误码
     * @return 返回更新后的校验者
     */
    public AppliableValidator<T, U> on(Predicate<? super T> predicate, String errorMsg, String errorCode) {
        this.checkValue();
        if (this.keepValidating() && predicate.test(this.value)) {
            this.setError(errorCode, errorMsg);
        }
        return this;
    }

    /**
     * 自定义校验方式,当满足条件时才进行校验
     * @param predicate 校验断言
     * @param errorMsg 错误信息
     * @param condition 校验的条件
     * @return 返回更新后的校验者
     */
    public AppliableValidator<T, U> onIf(Predicate<? super T> predicate, String errorMsg, Predicate<? super T> condition) {
        return this.onIf(predicate, errorMsg, NO_ERROR_CODE, condition);
    }

    /**
     * 自定义校验方式,当满足条件时才进行校验
     * @param predicate 校验断言
     * @param errorMsg 错误信息
     * @param condition 校验的条件
     * @param errorCode 错误码
     * @return 返回更新后的校验者
     */
    public AppliableValidator<T, U> onIf(Predicate<? super T> predicate,
                                       String errorMsg,
                                       String errorCode,
                                       Predicate<? super T> condition) {
        this.checkValue();
        if (this.keepValidating() && condition.test(this.value) && predicate.test(this.value)) {
            this.setError(errorCode, errorMsg);
        }
        return this;
    }

    /**
     * 当校验通过时执行的函数
     * @param successHandler 数据转化函数
     * @return 更新后的校验者
     * @throws DuplicateSuccessHandlerException 当执行函数被重复定义之时抛出该异常
     */
    public AppliableValidator<T, U> onSuccess(Function<T, U> successHandler) {
        if (this.successHandler != null) {
            throw new DuplicateSuccessHandlerException("The success mapper must be unique.");
        }
        this.successHandler = successHandler;
        return this;
    }

    /**
     * 当校验不通过时执行的函数
     * @param errorHandler 数据消费函数,会传入2个参数,第一个是被校验的参数,第二个是校验错误的信息
     * @return 返回函数的执行返回结果
     * @throws NullPointerException 当{@link #successHandler}未定义时抛出
     */
    public U onFailure(BiFunction<T, String, U> errorHandler) {
        if (this.successHandler == null) {
            throw new NullPointerException("The success consumer must not be null.");
        }
        this.checkValue();
        return this.isValid() ? this.successHandler.apply(this.value) : errorHandler.apply(this.value, this.getErrMsg());
    }
}
