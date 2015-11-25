package com.oneops.cms.simple.domain;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsRfcRelationSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

import static org.testng.Assert.*;

/**
 * this is an attempt to use relection to test bean classes without code, to see
 * they behave consistently in terms of construction and getters. More
 * refinements needed.
 */
public class BeanStandardTest {
	static Logger logger = Logger.getLogger(BeanStandardTest.class);
	private static final String KEY_STRING = "s";
	private static final String KEY_LONG = "l";
	private static final String KEY_INT = "i";
	private static final String KEY_BOOL = "b";
	private static final String DFLT_STRING = "A-strINg_valu#";
	private static final boolean DFLT_BOOLEAN = true;
	private static final int DFLT_INT = 12;
	private static final long DFLT_LONG = 97531L;

	/**
	 * given a key that goes along with a type of object we have one method here
	 * that gives back the default value useful for that object' setter
	 */
	private static class DefaultSetterArgs {

		/** key to default value */
		public static Object get(Object indexOf) {
			if (KEY_STRING.equals(indexOf)) {
				return DFLT_STRING;
			} else {
				if (KEY_LONG.equals(indexOf)) {
					return DFLT_LONG;
				} else {
					if (KEY_INT.equals(indexOf)) {
						return DFLT_INT;
					} else {
						if (KEY_BOOL.equals(indexOf)) {
							return DFLT_BOOLEAN;
						}
					}
				}
				return null;
			}
		}
	}

	/**
	 * holds they types we support String, Long, Int, and Boolean can add domain
	 * objects latere
	 */

	private static class DefaultTypes {
		/** is it supported? */
		public static boolean contains(Class type) {
			if (type.equals(String.class) || (type.equals(long.class))
					|| (type.equals(int.class)) || (type.equals(boolean.class))) {
				return true;
			} else {
				return false;
			}
		}

		/** what is its key so you can get default */
		public static Object indexOf(Class type) {
			if (type.equals(String.class)) {
				return KEY_STRING;
			} else {
				if (type.equals(long.class)) {
					return KEY_LONG;
				} else {
					if (type.equals(int.class)) {
						return KEY_INT;
					} else {
						if (type.equals(boolean.class)) {
							return KEY_BOOL;
						}
					}
				}
				return null;
			}
		}

	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		BeanStandardTest t = new BeanStandardTest();
		t.mutateAccessTest();
	}

	/**
	 * Mutate access test.
	 */
	@Test
	/**where we iterator over our bean subjects*/
	public void mutateAccessTest() {
		List<Object> beans = new ArrayList<Object>();
		beans.add(new CmsActionOrderSimple());
		beans.add(new CmsCIRelationSimple());
		beans.add(new CmsCISimple());
		beans.add(new CmsRfcCISimple());
		beans.add(new CmsRfcRelationSimple());
		beans.add(new CmsWorkOrderSimple());
		for (Object o : beans) {
			try {
				enforceStandards(o);

			} catch (SecurityException e) {
				logger.info("issue with type " + o);
			} catch (IllegalArgumentException e) {
				logger.info("issue with type " + o);
			} catch (NoSuchMethodException e) {
				logger.info("issue with type " + o);
			} catch (InstantiationException e) {
				logger.info("issue with type " + o);
			}
		}

	}

	/**
	 * use introspection to see what the class has for descriptors
	 */
	private void enforceStandards(Object target) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
			PropertyDescriptor[] descriptors = beanInfo
					.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				if (descriptor.getWriteMethod() == null) {
					continue;
				}
				if (descriptor.getPropertyType().isArray()) {
					continue;
				}
				enforceBehavior(target, descriptor.getDisplayName(), null);
			}
		} catch (IntrospectionException e) {
			logger.info("Failed while introspecting target "
					+ target.getClass());
		}

	}

	/**
			 * 
			 */
	public static void enforceBehavior(Object target, String property,
			Object argument) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException {
		try {
			logger.debug(" assertBasicGetterSetterBehavior target " + target);
			logger.debug(" , property " + property);

			PropertyDescriptor descriptor = new PropertyDescriptor(property,
					target.getClass());
			Object arg = argument;
			if (arg == null) {
				Class type = descriptor.getPropertyType();
				if (DefaultTypes.contains(type)) {
					// chose a default arg for this setter
					arg = DefaultSetterArgs.get(DefaultTypes.indexOf(type));
				} else {
					// arg =
					// ReflectionUtils.invokeDefaultConstructorEvenIfPrivate(type);
					Constructor c;
					try {
						c = type.getConstructor();
						// System.out.print(", got constructor: "+ c);
					} catch (NoSuchMethodException e) {
						return; // we have an object we do not know about, and
								// it has no default constructor
					}
					arg = type.newInstance();
				}
			}
			Method writeMethod = descriptor.getWriteMethod();
			Method readMethod = descriptor.getReadMethod();
			logger.debug(" about to writeMethod()   " + " -and arg- " + arg);
			writeMethod.invoke(target, arg);

			Object propertyValue = readMethod.invoke(target);

			// assertSame(property + " getter/setter failed test", arg,
			// propertyValue);
			logger.debug("==========propertyValue " + propertyValue);
			logger.debug("==========property " + property);
			logger.debug("==========arg " + arg);

			assertEquals(arg, propertyValue, " Error in class " + target
					+ " with " + property);

			// System.out.println("=propertyValue "+ propertyValue);

		} catch (IntrospectionException e) {
			String msg = "Error creating PropertyDescriptor for property ["
					+ property + "]. Do you have a getter and a setter?";
			logger.error(msg, e);
		} catch (IllegalAccessException e) {
			String msg = "Error accessing property. Are the getter and setter both accessible?";
			logger.error(msg, e);
		} catch (InvocationTargetException e) {
			String msg = "Error invoking method on target";
			logger.error(msg, e);
		}

	}

}
