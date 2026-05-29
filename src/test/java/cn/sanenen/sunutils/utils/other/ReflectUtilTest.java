package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ReflectUtilTest {

	@Test
	public void invokeSetterConvertsPrimitiveTypes() {
		PrimitiveTarget target = new PrimitiveTarget();

		ReflectUtil.invokeSetter(target, "count", "12");
		ReflectUtil.invokeSetter(target, "enabled", "true");

		assertEquals(12, target.getCount());
		assertTrue(target.isEnabled());
	}

	@Test
	public void invokeSetterSelectsCompatibleOverloadedSetter() {
		OverloadedTarget target = new OverloadedTarget();

		ReflectUtil.invokeSetter(target, "value", 7);

		assertEquals("integer:7", target.getSelected());
	}

	@Test
	public void invokeSetterConvertsDateValue() {
		DateTarget target = new DateTarget();

		ReflectUtil.invokeSetter(target, "created", "2024-01-02 03:04:05");

		assertNotNull(target.getCreated());
	}

	@Test
	public void getAccessibleMethodByNameWithArgsNumKeepsPrimitiveMatch() {
		assertEquals(int.class, ReflectUtil.getAccessibleMethodByName(new PrimitiveTarget(), "setCount", 1).getParameterTypes()[0]);
	}

	public static class PrimitiveTarget {
		private int count;
		private boolean enabled;

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public static class OverloadedTarget {
		private String selected;

		public void setValue(String value) {
			this.selected = "string:" + value;
		}

		public void setValue(Integer value) {
			this.selected = "integer:" + value;
		}

		public String getSelected() {
			return selected;
		}
	}

	public static class DateTarget {
		private Date created;

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			this.created = created;
		}
	}
}
