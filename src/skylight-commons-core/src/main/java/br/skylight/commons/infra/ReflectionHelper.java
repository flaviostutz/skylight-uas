package br.skylight.commons.infra;


public class ReflectionHelper {

	public static boolean instanceOfClasses(Class class1, Class[] instanceOfClasses) {
		if(class1==null) return false;
		if(instanceOfClasses==null) return false;
		boolean foundInterface = false;
		for (Class ifc : class1.getInterfaces()) {
			for (Class sc : instanceOfClasses) {
//				System.out.println(ifc + " " + sc);
				if(ifc.equals(sc)) {
					foundInterface = true;
					break;
				} else {
					for (Class ifs : ifc.getInterfaces()) {
						if(instanceOfClasses(ifs, instanceOfClasses)) {
							return true;
						}
					}
				}
			}
		}
		boolean foundSuperClass = false;
		for (Class sc : instanceOfClasses) {
//			System.out.println(sc + " " + class1.getSuperclass());
			if(sc.equals(class1.getSuperclass())) {
				foundSuperClass = true;
				break;
			} else {
				if(instanceOfClasses(class1.getSuperclass(), instanceOfClasses)) {
					return true;
				}
			}
		}
		return (foundSuperClass || foundInterface);
	}

	public static boolean hasDefaultConstructor(Class type) {
		try {
			type.getConstructor(new Class[]{});
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}
	
}
