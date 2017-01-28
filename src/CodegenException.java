// Do not modify the code below!  Don't add anything (including
// "public" declarations), don't remove anything. Don't wrap it in a
// package, don't make it an innner class of some other class.
// If your IDE suggsts to change anything below, ignore your IDE.

interface Codegen {
    public String codegen ( Program p ) throws CodegenException; }



class CodegenException extends Exception {
    public String msg;
    public CodegenException ( String _msg ) { msg = _msg; } }
