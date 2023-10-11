package cn.sanenen.sunutils.chain;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.log.Log;
import com.alibaba.fastjson.JSON;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;

import java.util.List;

/**
 * @author sun
 * @date 2020-12-21
 **/
public class SimpleMsgChain<T> implements IMsgChain<T> {
	private static final Log log = Log.get();
	private final Chain chain = new ChainBase();

	@Override
	public void setHandlers(List<IMsgHandler<T>> iMsgHandlers) {
		for (IMsgHandler<T> handler : iMsgHandlers) {
			chain.addCommand(new SimpleCommand<>(handler));
		}
	}

	@Override
	public void execute(T t) {
		ContextBase contextBase = new ContextBase();
		contextBase.put(IMsgChain.DATA_KEY, t);
		try {
			chain.execute(contextBase);
		} catch (Exception e) {
			log.error(e, "chain execute error. data:{}", JSON.toJSONString(t));
		} finally {
			contextBase.clear();
		}
	}

	private static class SimpleCommand<T> extends TypeReference<T> implements Command {
		private final IMsgHandler<T> handler;

		public SimpleCommand(IMsgHandler<T> handler) {
			this.handler = handler;
		}

		@Override
		public boolean execute(Context context) {
			return handler.process(MapUtil.get(context, IMsgChain.DATA_KEY, this));
		}
	}
}
