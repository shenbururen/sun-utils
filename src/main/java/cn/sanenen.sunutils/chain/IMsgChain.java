package cn.sanenen.sunutils.chain;

import java.util.List;

/**
 * @author sun
 * @date 2020-12-21
 **/
public interface IMsgChain<T> {
	String DATA_KEY = "DATA";

	void setHandlers(List<IMsgHandler<T>> handlers);

	void execute(T t) throws Exception;
}
