package br.skylight.commons.dli.gateways;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.plugin.ClasspathInspector;

@Test
public class AllSerializableStateMessagesTest {

	@Test(dataProvider = "serializableClasses")
	public void testAllSerializableStateMessages(Class class1) throws InstantiationException, IllegalAccessException, IOException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		testSerialization(class1);
	}
	
	@DataProvider(name="serializableClasses")
	private Class[][] getSerializableClassesForTesting() {
		List<Class> classes = ClasspathInspector.getMatchingClasses("br.skylight", new Class[]{SerializableState.class}, true, true, null, null);
		Class[][] result = new Class[classes.size()][1];
		for(int i=0; i<result.length; i++) {
			result[i] = new Class[] {classes.get(i)};
		}
		return result;
	}

	private void testSerialization(Class<? extends SerializableState> classType) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, SecurityException, NoSuchMethodException, InstantiationException {
		if(Modifier.isAbstract(classType.getModifiers())) {
			return;
		}
		SerializableState obj = classType.newInstance();
		//fill object with random data
		List<String> attributes = new ArrayList<String>();
		for (Method m : obj.getClass().getMethods()) {
			if(m.getParameterTypes().length==1) {
				Class type = m.getParameterTypes()[0];
				if(m.getReturnType()==Void.TYPE) {
					if(m.getName().startsWith("set")) {
						//get field name
						attributes.add(m.getName().substring(3));

						//string field
						if(type.equals(String.class)) {
							m.invoke(obj, "abcd"+ ((int)(Math.random()*10000)) +"efg");

						//enum field
						} else if(type.getEnumConstants()!=null) {
							m.invoke(obj, type.getEnumConstants()[0]);
			
						//int field
						} else if(type.equals(Integer.class) || type==Integer.TYPE) {
							m.invoke(obj, ((int)(Math.random()*254)));//254 to avoid problems with unsigned byte
						
						//float field
						} else if(type.equals(Float.class) || type==Float.TYPE) {
							m.invoke(obj, ((float)(Math.random()*10000)));
						
						//double field
						} else if(type.equals(Double.class) || type==Double.TYPE) {
							m.invoke(obj, ((double)(Math.random()*90)));//90 to avoid problems with Coordinates
						
						//try to instantiate a java bean
						} else {
							if(!Modifier.isAbstract(type.getModifiers())) {
								m.invoke(obj, type.newInstance());
							}
						}
					}
				}
			}
		}

		//write state to stream
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		obj.writeState(dos);
		
		//read state from stream
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		DataInputStream dis = new DataInputStream(bis);
		SerializableState tes = obj.getClass().newInstance();
		tes.readState(dis);

		//compare the two object's fields
		for (String attr : attributes) {
			Method m = null;
			try {
				m = tes.getClass().getMethod("get"+attr);
			} catch (NoSuchMethodException e) {
				try {
					m = tes.getClass().getMethod("is"+attr);
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
					break;
				}
			}
			Object original = m.invoke(obj);
			Object postStream = m.invoke(tes);
			assertTrue(original!=null);
			assertEquals(postStream, original, "Method " + m.getName() + "()");
		}
		
//		assertEquals(obj, tes);
	}
}
