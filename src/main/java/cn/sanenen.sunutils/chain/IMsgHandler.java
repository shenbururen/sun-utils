package cn.sanenen.sunutils.chain;

/**
 * @author sun
 * @date 2020-12-21
 **/
public interface IMsgHandler<T> {

	boolean process(T t);
}
