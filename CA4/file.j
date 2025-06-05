.class public CodeGeneration
.super java/lang/Object

.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method

.method public calculate ()I
    .limit stack 256
    .limit locals 256
    bipush 5
    istore 1
    bipush 10
    istore 2
    iload 2
    bipush 2
    bipush 3
    bipush 3
    idiv 
    irem 
    idiv 
    istore 2
    iload 2
    bipush 4
    iload 1
    isub 
    iadd 
    istore 2
    iload 2
    bipush 1
    isub 
    istore 1
    iload 1
    iload 1
    iload 1
    iload 2
    iload 2
    iload 2
    iload 1
    bipush 3
    iadd 
    idiv 
    imul 
    imul 
    imul 
    imul 
    imul 
    istore 1
    bipush 3
    bipush 56
    iload 1
    bipush 2
    idiv 
    isub 
    iadd 
    istore 1
    iload 1
    ireturn 
.end method

.method public static main([Ljava/lang/String;)V
    .limit stack 256
    .limit locals 256
    iconst_0
    istore 2
.end method

