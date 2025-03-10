package io.dongtai.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * API方法访问器，负责访问和处理方法中的API相关信息
 */
public class ApiMethodVisitor extends AdviceAdapter {
    private final String methodName;
    private final String className;
    private final String baseUrl;
    private String httpMethod = "";
    private String methodPath = "";
    private final Map<String, String> parameterInfo = new HashMap<>();
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("api.debug", "false"));
    
    protected ApiMethodVisitor(MethodVisitor mv, int access, String name, String descriptor, 
                             String className, String baseUrl) {
        super(Opcodes.ASM9, mv, access, name, descriptor);
        this.methodName = name;
        this.className = className;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // 处理方法级别的RequestMapping注解
        if (descriptor.contains("RequestMapping")) {
            httpMethod = "REQUEST";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        } else if (descriptor.contains("GetMapping")) {
            httpMethod = "GET";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        } else if (descriptor.contains("PostMapping")) {
            httpMethod = "POST";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        } else if (descriptor.contains("PutMapping")) {
            httpMethod = "PUT";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        } else if (descriptor.contains("DeleteMapping")) {
            httpMethod = "DELETE";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        } else if (descriptor.contains("PatchMapping")) {
            httpMethod = "PATCH";
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        }
        return super.visitAnnotation(descriptor, visible);
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        // 处理参数注解，如@RequestParam, @PathVariable等
        if (descriptor.contains("RequestParam")) {
            return new ParameterAnnotationVisitor(super.visitParameterAnnotation(parameter, descriptor, visible), 
                                                 parameter, "query");
        } else if (descriptor.contains("PathVariable")) {
            return new ParameterAnnotationVisitor(super.visitParameterAnnotation(parameter, descriptor, visible), 
                                                 parameter, "path");
        } else if (descriptor.contains("RequestBody")) {
            parameterInfo.put("param" + parameter, "body");
        } else if (descriptor.contains("RequestHeader")) {
            return new ParameterAnnotationVisitor(super.visitParameterAnnotation(parameter, descriptor, visible), 
                                                 parameter, "header");
        }
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }
    
    @Override
    protected void onMethodExit(int opcode) {
        // 方法结束时，如果收集到了API信息，则注册该API
        if (!httpMethod.isEmpty()) {
            String fullPath = baseUrl;
            if (!methodPath.isEmpty()) {
                if (!baseUrl.isEmpty() && !methodPath.startsWith("/")) {
                    fullPath += "/";
                }
                fullPath += methodPath;
            }
            
            // 注册API
            ApiCollector.registerApi(className, methodName, httpMethod, fullPath, parameterInfo);
            
            if (DEBUG) {
                System.out.println("[API Collector] Registered API: " + httpMethod + " " + fullPath + 
                                 " in " + className + "#" + methodName);
            }
        }
        super.onMethodExit(opcode);
    }
    
    /**
     * RequestMapping注解访问器
     */
    private class RequestMappingAnnotationVisitor extends AnnotationVisitor {
        public RequestMappingAnnotationVisitor(AnnotationVisitor av) {
            super(Opcodes.ASM9, av);
        }
        
        @Override
        public AnnotationVisitor visitArray(String name) {
            if (name.equals("value") || name.equals("path")) {
                return new AnnotationVisitor(Opcodes.ASM9, super.visitArray(name)) {
                    @Override
                    public void visit(String name, Object value) {
                        methodPath = value.toString();
                        super.visit(name, value);
                    }
                };
            } else if (name.equals("method")) {
                return new AnnotationVisitor(Opcodes.ASM9, super.visitArray(name)) {
                    @Override
                    public void visitEnum(String name, String descriptor, String value) {
                        if (descriptor.contains("RequestMethod")) {
                            httpMethod = value;
                        }
                        super.visitEnum(name, descriptor, value);
                    }
                };
            }
            return super.visitArray(name);
        }
        
        @Override
        public void visit(String name, Object value) {
            if ((name.equals("value") || name.equals("path")) && value instanceof String) {
                methodPath = (String) value;
            }
            super.visit(name, value);
        }
    }
    
    /**
     * 参数注解访问器
     */
    private class ParameterAnnotationVisitor extends AnnotationVisitor {
        private final int parameter;
        private final String paramType;
        private String paramName;
        
        public ParameterAnnotationVisitor(AnnotationVisitor av, int parameter, String paramType) {
            super(Opcodes.ASM9, av);
            this.parameter = parameter;
            this.paramType = paramType;
            this.paramName = "param" + parameter;
        }
        
        @Override
        public void visit(String name, Object value) {
            if (name.equals("value") || name.equals("name")) {
                paramName = value.toString();
            }
            super.visit(name, value);
        }
        
        @Override
        public void visitEnd() {
            parameterInfo.put(paramName, paramType);
            super.visitEnd();
        }
    }
}