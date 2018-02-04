package jaicore.planning.model.task.ceocstn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import jaicore.logic.fol.structure.Literal;

public class CEOCSTN2File {
	
	private static String packageName;
	
	public static void print(CEOCSTNPlanningProblem problem) {
		File output = null;
		//Filechooser for selecting the output-file
//		JFileChooser chooser = new JFileChooser();
//		chooser.showOpenDialog(null);
//		output = chooser.getSelectedFile();
		
		output = new File("C:\\Users\\Azoth\\Desktop\\Test.lisp");
		
		FileWriter fileWriter;
		BufferedWriter bw;
		try {
			fileWriter = new FileWriter(output);
			bw = new BufferedWriter(fileWriter);
			
			String fileName = output.getName();
			fileName = fileName.substring(0, fileName.indexOf("."));
			fileName = fileName.toLowerCase();
		
			
			//print the package if one is availiable
			if(packageName != "") {
				bw.write("(int-package : "+packageName +")");
				bw.flush();
			}
			
			//Writing of domain file
			bw.write("(defun define-" +fileName+"-domain()\n" );
			bw.write(indent(1) + "(let (( * define-silently* t))\n");
			bw.flush();
			
			bw.write(indent(2)+ "(defdomain (" +fileName+ " :redinfe-ok t)(\n");
			bw.flush();
			
			problem.getDomain().getOperations().stream().forEach(operation->{
				try {
//					System.out.println(operation);
					bw.write(indent(3) + "(:operator (!" +operation.getName());
					bw.flush();
				
					//print the parameter of the operation
					operation.getParams().stream().forEach(param->{
//						System.out.println(param);
						try {
							bw.write(" ?" + param.getName());
							bw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					});
					
					//adding the preconditions of the operation
					bw.write(")\n" + indent(6) +"(");
					operation.getPrecondition().forEach(precond->{
						try {
							bw.write(" (");
							bw.write(precond.getPropertyName());
							precond.getParameters().stream().forEach(param-> {
								try {
									bw.write(" ?"+ param.getName());
								} catch (IOException e) {
									
									e.printStackTrace();
								}
							});
							bw.write(")");
							bw.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						
					});
					bw.write(" )\n");
					bw.flush();
									
					//adding the delete list of the operation
					bw.write(indent(6) + "(");
					operation.getDeleteLists().values().stream().forEach(deleteList ->{
						deleteList.forEach(member -> {
						 try {
							bw.write(" (");
							bw.write(member.getProperty());
							member.getParameters().stream().forEach(param->{
								try {
									bw.write(" ?" + param.getName());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							bw.write(")");
							
							bw.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						});
					});
					
					bw.write(" )\n");
					
					
					//adding the add list of the operation
					bw.write(indent(6) + "(");
					operation.getAddLists().values().stream().forEach(deleteList ->{
						deleteList.forEach(member -> {
						 try {
							bw.write(" (");
							bw.write(member.getProperty());
							member.getParameters().stream().forEach(param->{
								try {
									bw.write(" ?" + param.getName());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							bw.write(")");
							
							bw.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						});
					});
					
					bw.write(" )\n");
					
					
					bw.write(indent(3) + " )\n\n");
					bw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
				
			//print the methods into the file
			problem.getDomain().getMethods().stream().forEach(method->{
				try {
					bw.write(indent(3)+"(:method (" + method.getName() );
					method.getParameters().stream().forEach(param -> {
						try {
							bw.write(" ?" + param.getName());
							bw.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					bw.write(")\n");
					
					//writes the preconditions of the method into the file
					bw.write(indent(6) + "(");
					method.getPrecondition().stream().forEach(precond->{
						try {
							bw.write(" (");
							bw.write(precond.getProperty());
							precond.getParameters().stream().forEach(param->{
								try {
									bw.write(" ?" + param.getName());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							bw.write(")");
							bw.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});					
					bw.write(")\n");
					bw.flush();
					
					//write the task of the method into the file
					bw.write(indent(6) + "(");
					Literal task = method.getTask();
					try {
						bw.write(" (");
						bw.write(task.getProperty());
						task.getParameters().stream().forEach(param->{
							try {
								bw.write(" ?" + param.getName());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						bw.write(")");
						bw.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
								
					bw.write(")\n");
					bw.flush();
					
					bw.write(indent(3) + ")\n\n");
					bw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			});		
			
			
			bw.write("))))");
			bw.flush();
			//closing the writer object
			fileWriter.close();
			bw.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	
	}
	
	//creates a number of intends;
	public static String indent(int numberOfIntends) {
		String r = "";
		for(int i = 0; i < numberOfIntends; i++) {
			r+= "\t";
		}
		return r;
	}
	
	public static void main(String[] args) {
		packageName = "";
		
		
		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0,0);
//		problem.getDomain().getOperations().stream().forEach(n-> System.out.println(n));
		print(problem);
	}
	

}
