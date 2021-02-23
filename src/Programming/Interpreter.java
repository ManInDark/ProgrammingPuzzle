package Programming;


import java.util.ArrayList;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;


public class Interpreter {

	private static MethodList methods;

	private static VariableList variables;

	public static Integer line;

	public static String sysoutin;

	public static String tabwith = "|---|  ";

	public Interpreter() {

	}

	public static void interpret() {
		String programm = "int te = sec + 5;\nprint(te);\nwhile (sec < te) {\n	delay(2);\n}";
		interpret(programm);
	}

	public static void interpret(AbstractDocument document) {
		try {
			interpret(document.getText(0,document.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public static void interpret(String str) {
		try {
			init();
		} catch (UnsupportetVariableNameExeption | UnsupportetMethodNameExeption | InvalidValueException
			| WrongTypeException e) {
			System.err.println("init gone wrong (some idiot added duplicate metods or attributes):" + e.getMessage());
			return;
		}

		CustStr text = new CustStr(str);

		System.out.println("interpreting:\n" + text + "\n\n");

		String exeption = "";

		line = 1;

		while (text.val.length() > 0) {
			try {
				interpretblock(text,tabwith);
			} catch (CustomExeption e) {
				// e.printStackTrace();
				exeption = e.getMessage();
				break;
			}
		}
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
		}
		if (exeption != "")
			System.err.println(exeption);
		else {
			System.out.println("\n" + variables);

			System.out.println("\n" + methods);
		}
	}

	public static void init()
		throws UnsupportetVariableNameExeption,UnsupportetMethodNameExeption,InvalidValueException,WrongTypeException {
		methods = new MethodList();
		variables = new VariableList();

		methods.addMethod(new print());

		methods.addMethod(new move());

		methods.addMethod(new toString());

		methods.addMethod(new delay());

		methods.addMethod(new exit());

		methods.addMethod(new loopbreak());

		variables.addVariable(new MY_int("sec") {

			@Override
			public Integer getValue() {
				return (int) (System.currentTimeMillis() * 0.001);
			}

		});

	}

	/**
	 * @param text
	 * @throws CustomExeption
	 */
	public static void interpretblock(CustStr text,String sysoutin) throws CustomExeption {
		Interpreter.sysoutin = sysoutin;
		if (text.val.isBlank()) {
			text.val = "";
			return;
		}
		System.out.println(sysoutin + "##################");

		if (StrUtils.startswith(StrUtils.fullstrip(text.val),Keywords.values())) { // keyword
			String part = getSection(StrUtils.fullstrip(text.val),false);
			if (StrUtils.count(part,"\n") > 0)
				line += StrUtils.count(part,"\n");
			text.val = StrUtils.removetext(text.val,part + ";");
			part = StrUtils.fullstrip(part);
			System.out.println(sysoutin + "process: " + StrUtils.removespace(part));

			String name = StrUtils.first(part,'(');
			System.out.println(sysoutin + "name: " + name);
			Keyword keyword = null;

			String inner = getSection(part,true).substring(1,getSection(part,true).length() - 1);
			System.out.println(sysoutin + "inner block: " + StrUtils.removespace(inner));
			switch (Keywords.convert(name)) {
				case MY_if:
					keyword = new MY_if(inner);
				break;
				case MY_function:
					keyword = new MY_function(inner);
				break;
				case MY_while:
					keyword = new MY_while(inner);
				break;
			}
			String parameters = StrUtils.fromto(part,'(',')');
			ArrayList<Object> newparams;
			Interpreter.sysoutin = Interpreter.sysoutin + Interpreter.tabwith;
			do {
				ArrayList<String> params = StrUtils.parts(parameters,',',true);
				newparams = new ArrayList<>();
				if (params.size() != keyword.getParametersize())
					throw new MethodParametersExeption(params.size(),keyword.getParametersize(),line);
				for (String param : params) {
					if (param.contains("+") || param.contains("-") || param.contains("*") || param.contains("/")) {
						newparams.add(params.indexOf(param),calculate(param));
					} else if (param.contains("=") || param.contains(">") || param.contains("<")
						|| param.contains("!")) {
						newparams.add(params.indexOf(param),booleanate(param));
					} else if (variables.get(param) != null) {
						newparams.add(params.indexOf(param),variables.get(param));
					} else {
						newparams.add(param);
					}
				}
			} while (keyword.execute(newparams));
		} else {
			String part = StrUtils.part(text.val,';',0);
			if (StrUtils.count(part,"\n") > 0)
				line += StrUtils.count(part,"\n");
			text.val = StrUtils.removetext(text.val,part + ";");
			part = StrUtils.fullstrip(part);
			System.out.println(sysoutin + "process: " + part);
			if (!part.contains("=") && part.contains("(")) { // Method
				String firs = StrUtils.first(part,'(');
				Method method = methods.get(firs);
				if (method != null) {
					System.out.println(sysoutin + "method: " + method);
					String parameters = StrUtils.fromto(part,'(',')');
					ArrayList<String> params = StrUtils.parts(parameters,',',true);
					ArrayList<Object> newparams = new ArrayList<>();
					if (params.size() != method.getParametersize())
						throw new MethodParametersExeption(params.size(),method.getParametersize(),line);
					for (String param : params) {
						if (param.contains("+") || param.contains("-") || param.contains("*") || param.contains("/")) {
							newparams.add(params.indexOf(param),calculate(param));
						} else if (param.contains("=") || param.contains(">") || param.contains("<")
							|| param.contains("!")) {
							newparams.add(params.indexOf(param),booleanate(param));
						} else if (variables.get(param) != null) {
							newparams.add(params.indexOf(param),variables.get(param));
						} else {
							newparams.add(param);
						}
					}
					method.execute(newparams);
				} else {
					throw new MethodNotFoundExeption(firs,line);
				}
			} else { // Var definition
				String typestr = StrUtils.first(part,' ');
				Datatypes type = Datatypes.contains(typestr);
				if (type != null) {
					System.out.println(sysoutin + "datatype: " + type);
					if (StrUtils.parts(StrUtils.part(part,'=',0),' ',true).size() > 2)
						throw new VariableDeclarationExeption(part,line);
					String name = StrUtils.part(part,' ',1);
					System.out.println(sysoutin + "name: " + name);
					if (checkname(name))
						throw new UnsupportetVariableNameExeption(name,line);
					if (part.contains("=")) {
						String value = StrUtils.part(part,'=',1);
						Variable<?> set = variables.get(value);
						Method met = methods.get(StrUtils.first(value,'('));
						if (set != null) {
							value = set.getValue().toString();
							System.out.println(sysoutin + "value from variable: " + set);
						} else if (met != null) {
							System.out.println(sysoutin + "method: " + met);
							String parameters = StrUtils.fromto(part,'(',')');
							ArrayList<String> params = StrUtils.parts(parameters,',',true);
							ArrayList<Object> newparams = new ArrayList<>();
							if (params.size() != met.getParametersize())
								throw new MethodParametersExeption(params.size(),met.getParametersize(),line);
							for (String param : params) {
								if (param.contains("+") || param.contains("-") || param.contains("*")
									|| param.contains("/")) {
									newparams.set(params.indexOf(param),calculate(param));
								} else if (param.contains("=") || param.contains(">") || param.contains("<")
									|| param.contains("!")) {
									newparams.add(params.indexOf(param),booleanate(param));
								} else if (variables.get(param) != null) {
									newparams.set(params.indexOf(param),variables.get(param));
								}
							}
							value = met.execute(newparams).getType().toString();
						} else if (part.contains("+") || part.contains("-") || part.contains("*")
							|| part.contains("/")) {
							value = calculate(value);
							System.out.println(sysoutin + "value: " + value);
						} else if (part.contains("=") || part.contains(">") || part.contains("<")
							|| part.contains("!")) {
							value = booleanate(value);
							System.out.println(sysoutin + "value: " + value);
						} else
							System.out.println(sysoutin + "value: " + value);
						Variable<?> variable = null;
						switch (type) {
							case MY_int:
								variable = new MY_int(value,name);
							break;
							case MY_boolean:
								variable = new MY_boolean(value,name);
							break;
							case MY_String:
								variable = new MY_String(value,name);
							break;
							case MY_double:
								variable = new MY_double(value,name);
							break;
						}
						variables.add(variable);
					} else {
						System.out.println(sysoutin + "value: " + "no value");
						Variable<?> variable = null;
						switch (type) {
							case MY_int:
								variable = new MY_int(name);
							break;
							case MY_boolean:
								variable = new MY_boolean(name);
							break;
							case MY_String:
								variable = new MY_String(name);
							break;
							case MY_double:
								variable = new MY_double(name);
							break;
						}
						variables.add(variable);
					}

				} else
					throw new InvalidDatatypeExeption(typestr,line);
			}
		}
		System.out.println(sysoutin + "##################\n");
	}

	private static String getSection(String text,boolean inner) throws BracketExeption {
		int bracketcounter = 0;
		boolean firstfound = false;
		char[] chars = new char[text.length()];
		text.getChars(0,text.length(),chars,0);
		String builder = "";
		for (char ch : chars) {
			if (ch == '{') {
				firstfound = true;
				bracketcounter++;
			} else if (ch == '}') {
				bracketcounter--;
			}
			if (!(inner && !firstfound))
				builder += ch;
			if (firstfound && bracketcounter == 0)
				return builder;
		}
		throw new BracketExeption(bracketcounter,line);
	}

	private static String calculate(String inp) throws CalculationExeption {
		inp = inp.replace(" ","");
		String builder = "";
		String operator = "+";
		String solution = "";
		for (int i = 0; i < inp.length(); i++) {
			char c = inp.charAt(i);
			if (c == '+' || c == '-' || c == '*' || c == '/' || i == inp.length() - 1) {
				if (i == inp.length() - 1)
					builder += c;
				if (builder.isBlank()) {
					builder += c;
					continue;
				}
				Variable<?> variable = variables.get(builder);
				if (variable != null) {
					builder = variable.getValue().toString();
				}
				// System.out.print(solution + operator + builder + "=");
				if (operator == "") {
					solution = builder;
				} else {
					if (operator.equals("+")) {
						solution = add(solution,builder);
					} else if (operator.equals("*")) {
						solution = mulitiply(solution,builder);
					} else if (operator.equals("-")) {
						solution = subract(solution,builder);
					} else if (operator.equals("/")) {
						solution = divide(solution,builder);
					}
				}
				// System.out.println(solution);
				builder = "";
				operator = "" + c;
			} else {
				builder += c;
			}
		}
		return solution.toString();
	}

	private static String add(String f,String l) {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi + li);
			} catch (NumberFormatException e) {
			}
		} catch (NumberFormatException e) {
		}
		if (f.startsWith("\"") && f.endsWith("\""))
			f = f.substring(1,f.length() - 1);
		if (l.startsWith("\"") && l.endsWith("\""))
			l = l.substring(1,l.length() - 1);
		return f + l;
	}

	private static String subract(String f,String l) throws CalculationExeption {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi - li);
			} catch (NumberFormatException e) {
				throw new CalculationExeption(f + "-" + l,"-",Datatypes.MY_double,Datatypes.MY_String,line);
			}
		} catch (NumberFormatException e) {
			try {
				double li = Double.parseDouble(l);
				return f.substring(0,(int) (f.length() - li));
			} catch (NumberFormatException e2) {
				throw new CalculationExeption(f + "-" + l,"-",Datatypes.MY_String,Datatypes.MY_String,line);
			}
		}
	}

	private static String mulitiply(String f,String l) throws CalculationExeption {
		try {
			double fi = Double.parseDouble(f); // f int
			try {
				double li = Integer.parseInt(l); // l int
				return String.valueOf(fi * li);
			} catch (NumberFormatException e) { // f int l not
				if (l.startsWith("\"") && l.endsWith("\""))
					l = f.substring(1,l.length() - 1);
				String build = "";
				for (int i = 0; i < fi; i++) {
					build += l;
				}
				return build;
			}
		} catch (NumberFormatException e) { // f no int
			try {
				double li = Double.parseDouble(l); // l int f not
				if (f.startsWith("\"") && f.endsWith("\""))
					f = f.substring(1,f.length() - 1);
				String build = "";
				for (int i = 0; i < li; i++) {
					build += f;
				}
				return build;
			} catch (NumberFormatException e1) { // l and f no int
				throw new CalculationExeption(f + "*" + l,"*",Datatypes.MY_String,Datatypes.MY_String,line);
			}
		}

	}

	private static String divide(String f,String l) throws CalculationExeption {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi / li);
			} catch (Exception e) {
				throw new CalculationExeption(f + "/" + l,"/",Datatypes.MY_double,Datatypes.MY_String,line);
			}
		} catch (NumberFormatException e) {
			try {
				Double.parseDouble(l);
				throw new CalculationExeption(f + "/" + l,"/",Datatypes.MY_String,Datatypes.MY_double,line);
			} catch (NumberFormatException e2) {
				throw new CalculationExeption(f + "/" + l,"/",Datatypes.MY_String,Datatypes.MY_String,line);
			}
		}
	}

	private static String booleanate(String inp) throws CalculationExeption {
		inp = inp.replace(" ","");
		String builder = "";
		String operator = "";
		String solution = "";
		for (int i = 0; i < inp.length(); i++) {
			char c = inp.charAt(i);
			if (c == '=' || c == '>' || c == '<' || c == '!' || i == inp.length() - 1) {
				if (i == inp.length() - 1)
					builder += c;
				Variable<?> variable = variables.get(builder);
				if (variable != null) {
					builder = variable.getValue().toString();
				}
				// System.out.print(solution + operator + builder + "=");
				if (operator == "") {
					solution = builder;
				} else {
					if (operator.equals(">")) {
						solution = bigger(solution,builder);
					} else if (operator.equals("<")) {
						solution = smaller(solution,builder);
					} else if (operator.equals("=>")) {
						solution = biggerequals(solution,builder);
					} else if (operator.equals("=<")) {
						solution = smallerequals(solution,builder);
					} else if (operator.equals("==")) {
						solution = equals(solution,builder);
					} else if (operator.equals("=!")) {
						solution = notequals(solution,builder);
					} else if (operator.equals("!")) {
						solution = not(builder);
					} else {
						operator += c;
					}
				}
				// System.out.println(solution);
				builder = "";
				operator = "" + c;
			} else {
				builder += c;
			}
		}
		return solution;
	}

	private static String equals(String f,String l) {
		if (f.startsWith("\"") && f.endsWith("\""))
			f = f.substring(1,f.length() - 1);
		if (l.startsWith("\"") && l.endsWith("\""))
			l = l.substring(1,l.length() - 1);
		return String.valueOf(f == l);
	}

	private static String notequals(String f,String l) {
		if (f.startsWith("\"") && f.endsWith("\""))
			f = f.substring(1,f.length() - 1);
		if (l.startsWith("\"") && l.endsWith("\""))
			l = l.substring(1,l.length() - 1);
		return String.valueOf(f != l);
	}

	private static String bigger(String f,String l) {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi > li);
			} catch (NumberFormatException e) {
				return String.valueOf(fi > l.length());
			}
		} catch (NumberFormatException e) {
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(f.length() > li);
			} catch (NumberFormatException e2) {
				return String.valueOf(f.length() > l.length());
			}
		}
	}

	private static String smaller(String f,String l) {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi < li);
			} catch (NumberFormatException e) {
				return String.valueOf(fi < l.length());
			}
		} catch (NumberFormatException e) {
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(f.length() < li);
			} catch (NumberFormatException e2) {
				return String.valueOf(f.length() < l.length());
			}
		}

	}

	private static String smallerequals(String f,String l) {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi <= li);
			} catch (NumberFormatException e) {
				return String.valueOf(fi <= l.length());
			}
		} catch (NumberFormatException e) {
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(f.length() <= li);
			} catch (NumberFormatException e2) {
				return String.valueOf(f.length() <= l.length());
			}
		}

	}

	private static String biggerequals(String f,String l) {
		try {
			double fi = Double.parseDouble(f);
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(fi >= li);
			} catch (NumberFormatException e) {
				return String.valueOf(fi >= l.length());
			}
		} catch (NumberFormatException e) {
			try {
				double li = Double.parseDouble(l);
				return String.valueOf(f.length() >= li);
			} catch (NumberFormatException e2) {
				return String.valueOf(f.length() >= l.length());
			}
		}

	}

	private static String not(String l) throws CalculationExeption {
		try {
			return String.valueOf(!Boolean.parseBoolean(l));
		} catch (NumberFormatException e) {
			throw new CalculationExeption("!" + l,"!",Datatypes.notype,Datatypes.MY_String,line);
		}
	}

	private static boolean checkname(String name) {
		return Keywords.contains(name) != null || Datatypes.contains(name) != null || name.contains("+")
			|| name.contains("-") || name.contains("*") || name.contains("/");
	}

}



class CustStr {

	public String val;

	public CustStr(String text) {
		val = text;
	}

	@Override
	public String toString() {
		return val;
	}

}



class StrUtils {

	public static String getText(AbstractDocument document) {
		try {
			return document.getText(0,document.getLength());
		} catch (BadLocationException e) {
			return "";
		}
	}

	public static String fullstrip(String part) {
		part = part.strip();
		char[] chars = new char[part.length()];
		part.getChars(0,part.length(),chars,0);
		String out = "";
		for (char ch : chars) {
			if (!(ch == '\n' || ch == '\t'))
				out += ch;
			else
				out += " ";
		}
		return out;
	}

	public static String removespace(String part) {
		part = fullstrip(part);
		char[] chars = new char[part.length()];
		part.getChars(0,part.length(),chars,0);
		String out = "";
		int spacecounter = 0;
		for (char ch : chars) {
			if (ch == ' ') {
				spacecounter++;
				if (!(spacecounter > 1)) {
					out += ch;
					spacecounter = 0;
				}
			} else {
				out += ch;
			}
		}
		return out;
	}

	public static boolean startswith(String text,Keywords...keywords) {
		for (Keywords keyword : keywords) {
			if (text.startsWith(Keywords.convert(keyword)))
				return true;
		}
		return false;
	}

	public static String removetext(String text,String ret) {
		return text.strip().substring(Math.min(ret.strip().length(),text.strip().length()),text.strip().length());
	}

	public static String first(String text,char split) {
		char[] chars = new char[text.length()];
		text.getChars(0,text.length(),chars,0);
		String out = "";
		for (char ch : chars) {
			if (ch != split)
				out += ch;
			else
				return fullstrip(out);
		}
		return fullstrip(text);
	}

	public static ArrayList<String> parts(String text,char split,boolean strip) {
		char[] chars = new char[text.length()];
		text.getChars(0,text.length(),chars,0);
		String build = "";
		ArrayList<String> parts = new ArrayList<>();
		for (char ch : chars) {
			if (ch != split)
				build += ch;
			else {
				if (build.length() > 0)
					if (strip)
						parts.add(fullstrip(build));
					else
						parts.add(build);
				build = "";
			}
		}
		if (strip)
			parts.add(fullstrip(build));
		else
			parts.add(build);
		build = "";
		return parts;
	}

	public static String part(String text,char split,int ind) {
		return parts(text,split,true).get(ind);
	}

	public static String fromto(String text,char start,char end) {
		char[] chars = new char[text.length()];
		text.getChars(0,text.length(),chars,0);
		String out = "";
		boolean started = false;
		for (char ch : chars) {
			if (started)
				if (ch != end)
					out += ch;
				else
					return out;
			else if (ch == start)
				started = true;
		}
		return text;
	}

	public static int count(String text,String split) {
		int count = 0;
		char[] chars = new char[text.length()];
		text.getChars(0,text.length(),chars,0);
		char[] test = new char[split.length()];
		split.getChars(0,split.length(),test,0);
		int testcounter = 0;
		for (char ch : chars) {
			if (ch == test[testcounter]) {
				testcounter++;
				if (testcounter == test.length) {
					count++;
					testcounter = 0;
				}
			}
		}
		return count;
	}

}