package Programming;


import java.util.ArrayList;


enum Datatypes {

	notype,alltypes,MY_int,MY_String,MY_boolean,MY_double;

	static Datatypes convert(String str) {
		switch (str) {
			case "String":
				return MY_String;
			case "boolean":
				return MY_boolean;
			case "int":
				return MY_int;
			case "double":
				return MY_double;
		}
		return null;
	}

	static String convert(Datatypes datatype) {
		switch (datatype) {
			case MY_String:
				return "String";
			case MY_boolean:
				return "boolean";
			case MY_int:
				return "int";
			case MY_double:
				return "double";
			case alltypes:
				return "alltypes";
			case notype:
				return "notypes";
		}
		return null;
	}

}



abstract class Variable<T> {

	protected Datatypes type;

	private T value;

	protected String name;

	public Variable(String name,Datatypes type) {
		this.name = name;
		this.type = type;
	}

	public void changeValue(String object) throws InvalidValueException,WrongTypeException {
		T newvalue = checkvalue(object);
		if (newvalue != null)
			setValue(newvalue);
	}

	private void setValue(T value) {
		this.value = value;
	}

	abstract T checkvalue(String test) throws InvalidValueException,WrongTypeException;

	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Name: " + name + "  type: " + Datatypes.convert(type) + "  value: " + getValue();
	}

	public String getName() {
		return name;
	}

	public Datatypes getType() {
		return type;
	}

}



class MY_String extends Variable<String> {

	/**
	 * Declaration without parameters
	 * 
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_String(String name) throws InvalidValueException,WrongTypeException {
		this("",name);
	}

	/**
	 * Decalration with parameters or from other variable
	 * 
	 * @param value
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_String(String value,String name) throws InvalidValueException,WrongTypeException {
		super(name,Datatypes.MY_String);
		changeValue(value);

	}

	@Override
	public String checkvalue(String test) throws InvalidValueException {
		if (test.startsWith("\"") && test.endsWith("\""))
			test = test.substring(1,test.length() - 1);
		if (test.length() >= 0 && test.length() < 2147483647)
			return test;
		throw new InvalidValueException(0,2147483647,test.length(),3);
	}

}



class MY_boolean extends Variable<Boolean> {

	/**
	 * Declaration without parameters
	 * 
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_boolean(String name) throws InvalidValueException,WrongTypeException {
		this("false",name);
	}

	/**
	 * Decalration with parameters or other variable
	 * 
	 * @param value
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_boolean(String value,String name) throws InvalidValueException,WrongTypeException {
		super(name,Datatypes.MY_boolean);
		changeValue(value);
	}

	@Override
	public Boolean checkvalue(String test) throws InvalidValueException,WrongTypeException {
		Boolean newvalue = null;
		if (test.equals("true"))
			newvalue = true;
		else if (test.equals("false"))
			newvalue = false;
		else
			throw new WrongTypeException(type,test,3);
		if (newvalue == true || newvalue == false)
			return newvalue;
		throw new InvalidValueException(false,true,test,3);
	}

}



class MY_int extends Variable<Integer> {

	/**
	 * Declaration without parameters
	 * 
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_int(String name) throws InvalidValueException,WrongTypeException {
		this("0",name);
	}

	/**
	 * Decalration with parameters or from other variable
	 * 
	 * @param value
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_int(String value,String name) throws InvalidValueException,WrongTypeException {
		super(name,Datatypes.MY_int);
		changeValue(value);
	}

	@Override
	public Integer checkvalue(String test) throws InvalidValueException,WrongTypeException {
		try {
			Integer newvalue = Integer.parseInt(test);
			return newvalue;
		} catch (NumberFormatException e) {
			try {
				Double double1 = Double.parseDouble(test);
				return double1.intValue();
			} catch (NumberFormatException e2) {
				try {
					Long.parseLong(test);
				} catch (NumberFormatException e3) {
					throw new WrongTypeException(type,test,3);
				}
				throw new InvalidValueException(Integer.MIN_VALUE,Integer.MAX_VALUE,test,3);
			}

		}

	}

}



class MY_double extends Variable<Double> {

	/**
	 * Declaration without parameters
	 * 
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_double(String name) throws InvalidValueException,WrongTypeException {
		this("0",name);
	}

	/**
	 * Decalration with parameters or from other variable
	 * 
	 * @param value
	 * @param name
	 * @throws WrongTypeException
	 * @throws InvalidValueException
	 */
	public MY_double(String value,String name) throws InvalidValueException,WrongTypeException {
		super(name,Datatypes.MY_double);
		changeValue(value);
	}

	@Override
	public Double checkvalue(String test) throws WrongTypeException {
		try {
			Double newvalue = Double.parseDouble(test);
			return newvalue;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new WrongTypeException(type,test,3);
		}
	}

}



class VariableList extends ArrayList<Variable<?>> {

	public VariableList() {
		super();
	}

	public Variable<?> get(String name) {
		name = name.strip();
		for (Variable<?> variable : this) {
			if (variable.getName().equals(name))
				return variable;
		}
		return null;
	}

	/**
	 * tests if method with same name alread exists
	 * 
	 * @param method
	 * @throws UnsupportetVariableNameExeption
	 */
	public boolean addVariable(Variable<?> variable) throws UnsupportetVariableNameExeption {
		Variable<?> old = get(variable.getName());
		if (old == null) {
			return super.add(variable);
		}
		throw new UnsupportetVariableNameExeption(variable,"duplicate name ",Interpreter.line);
	}

	@Override
	public boolean add(Variable<?> e) {
		throw new RuntimeException();
	}

	@Override
	public String toString() {
		String ret = "VariableList:\n";
		for (Variable<?> var : this) {
			ret += "    " + var.toString() + "\n";
		}
		return ret;
	}

}
