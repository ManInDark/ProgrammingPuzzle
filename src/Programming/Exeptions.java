package Programming;


class CustomExeption extends Exception {

	public CustomExeption(String exeption,int line) {
		super(exeption + "  \nin Line: " + line);
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		String message = getLocalizedMessage();
		return "\n" + s + ":\n" + message;
	}

}



class InvalidValueException extends CustomExeption {

	public InvalidValueException(Object min,Object max,Object object,int line) {
		super(object + " was out of Range from " + min + " to " + max,line);
	}

}



class InvalidDatatypeException extends CustomExeption {

	public InvalidDatatypeException(Object object,int line) {
		super(object + " was no valid Datatype",line);
	}

}



class WrongTypeException extends CustomExeption {

	public WrongTypeException(Datatypes type,Object object,int line) {
		super(object.toString() + " did not match Type " + Datatypes.convert(type),line);
	}

}



class VariableDeclarationExeption extends CustomExeption {

	public VariableDeclarationExeption(String declaration,int line) {
		super("Variabledeclaration not allowed : " + declaration,line);
	}

}



class UnsupportetVariableNameExeption extends CustomExeption {

	public UnsupportetVariableNameExeption(String name,int line) {
		super("Variablename not supported : " + name,line);
	}

	public UnsupportetVariableNameExeption(Variable<?> variable,String extrainfo,int line) {
		super(extrainfo + " : " + variable,line);
	}

}



class UnsupportetMethodNameExeption extends CustomExeption {

	public UnsupportetMethodNameExeption(String name,int line) {
		super("Methodname not supported : " + name,line);
	}

	public UnsupportetMethodNameExeption(Method variable,String extrainfo,int line) {
		super(extrainfo + " : " + variable,line);
	}

}



class CalculationExeption extends CustomExeption {

	public CalculationExeption(String calucation,int line) {
		super("error calculation: " + calucation,line);
	}

}



class MethodNotFoundExeption extends CustomExeption {

	public MethodNotFoundExeption(String name,int line) {
		super("method not found: " + name,line);
	}

}



class MethodParametersExeption extends CustomExeption {

	public MethodParametersExeption(int given,int wanted,int line) {
		super("method wanted " + wanted + " parameters, " + given + " were given",line);
	}

}



class WrongParameterTypeExeption extends CustomExeption {

	public WrongParameterTypeExeption(String given,Datatypes wanted,int place,int line) {
		super("method wanted " + Datatypes.convert(wanted) + " at place " + place + " but got " + given,line);
	}

}
