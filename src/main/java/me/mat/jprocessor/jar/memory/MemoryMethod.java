package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import me.mat.jprocessor.util.asm.IAccessed;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MemoryMethod extends MemoryAnnotatedElement implements IAccessed {

    public final List<MemoryLocalVariable> localVariables = new ArrayList<>();

    @NonNull
    public MemoryClass parent;

    @NonNull
    private MethodNode methodNode;

    public MemoryInstructions instructions;

    public MemoryClass baseClass = null;
    public MemoryMethod baseMethod = null;

    /**
     * Loads all the annotation for the current method
     *
     * @param classes map of all the loaded classes
     * @return {@link MemoryMethod}
     */

    public MemoryMethod init(Map<String, MemoryClass> classes) {
        // define new memory instructions for the current method
        this.instructions = new MemoryInstructions(this, methodNode.instructions);

        // initialize all the annotations
        this.init(methodNode.visibleAnnotations, classes);

        // if the local variables are valid
        if (methodNode.localVariables != null) {

            // loop through all the variables and load them into the memory
            methodNode.localVariables.forEach(localVariableNode
                    -> localVariables.add(new MemoryLocalVariable(localVariableNode)));
        }

        // return the instance of the field
        return this;
    }

    /**
     * Checks if this method overrides the provided method
     *
     * @param baseClass  class the that method is from
     * @param baseMethod method that you want to check against
     */

    protected void checkForOverride(MemoryClass baseClass, MemoryMethod baseMethod) {
        if (equals(baseMethod)) {
            this.baseClass = baseClass;
            this.baseMethod = baseMethod;
        }
    }

    /**
     * Checks if the method can be remapped
     *
     * @return {@link Boolean}
     */

    public boolean isChangeable() {
        return ASMUtil.isChangeable(methodNode) && !isMainMethod();
    }

    /**
     * Checks if the method overrides a method
     * from one of the super classes
     *
     * @return {@link Boolean}
     */

    public boolean isOverride() {
        return baseClass != null && baseMethod != null;
    }

    /**
     * Checks if the method is a main method
     *
     * @return {@link Boolean}
     */

    public boolean isMainMethod() {
        return methodNode.name.equals("main") && methodNode.desc.equals("([Ljava/lang/String;)V");
    }

    /**
     * Gets the name of the current method
     *
     * @return {@link String}
     */

    public String name() {
        return methodNode.name;
    }

    /**
     * Gets the description of the current method
     *
     * @return {@link String}
     */

    public String description() {
        return methodNode.desc;
    }

    /**
     * Returns all the instructions in the method
     *
     * @return {@link InsnList}
     */

    public InsnList getInstructions() {
        return methodNode.instructions;
    }

    /**
     * Gets the access of the method
     *
     * @return {@link Integer}
     */

    public int getAccess() {
        return methodNode.access;
    }

    /**
     * Sets the access of the method
     *
     * @param access access that you want to set it to
     */

    public void setAccess(int access) {
        methodNode.access = access;
    }

    /**
     * Checks if the provided object is equal to this object
     *
     * @param obj object that you are trying to check
     * @return {@link Boolean}
     */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodNode) {
            return ASMUtil.isSameMethod((MethodNode) obj, methodNode);
        } else if (!(obj instanceof MemoryMethod)) {
            return false;
        }
        return ASMUtil.isSameMethod(((MemoryMethod) obj).methodNode, methodNode);
    }

}
