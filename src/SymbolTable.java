import javax.swing.text.Segment;
import java.awt.*;
import java.util.Hashtable;

public class SymbolTable {

    public enum kind{
        STATIC(VMWriter.segment.STATIC), FIELD(VMWriter.segment.THIS), ARG(VMWriter.segment.ARGUMENT), VAR(VMWriter.segment.LOCAL), NONE(null);

        final VMWriter.segment segment;

        kind(VMWriter.segment segment){
            this.segment = segment;
        }


        public static kind convertStaticField(keyWord type) throws Exception {
            switch (type){
                case FIELD:
                    return kind.FIELD;
                case STATIC:
                    return kind.STATIC;
                default:
                    throw new Exception("This is not a field or static type");
            }
        }
    }

    //for class and field scoped identifiers
    private Hashtable<kind, Integer> indexTable;


    private Hashtable<String, String> typeTable;
    private Hashtable<String, kind> kindTable;
    private Hashtable<String, Integer> indexOfTable;


    //for subroutines
    private Hashtable<String, String> subTypeTable;
    private Hashtable<String, kind> subKindTable;
    private Hashtable<String, Integer> subIndexOfTable;
    /**
     * Creates a new symbol table
     */
    SymbolTable(){
        typeTable = new Hashtable<>();
        kindTable = new Hashtable<>();
        indexOfTable = new Hashtable<>();
        indexTable = new Hashtable<>();

        subTypeTable = new Hashtable<>();
        subKindTable = new Hashtable<>();
        subIndexOfTable = new Hashtable<>();

        setIndexTable(indexTable);
    }

    private void setIndexTable(Hashtable<kind, Integer> table){
        for (kind kind : kind.values()){
            table.put(kind, 0);
        }
    }

    /**
     * Starts a new subroutine scope(i.e., resets the subroutine's symbol table).
     */
    public void startSubroutine(){
        indexTable.put(kind.ARG, 0);
        indexTable.put(kind.VAR, 0);

        subTypeTable = new Hashtable<>();
        subKindTable = new Hashtable<>();
        subIndexOfTable = new Hashtable<>();
    }

    /**
     * Defines a new identifier of the given name, type, and kind, and assignss it a running index.
     * STATIC and FIELD identifiers have a class scope while ARG and VAR identifiers have a
     * subroutine scope.
     * @param name name of variable
     * @param type type of variable
     * @param kind STATIC, FIELD, ARG or VAR
     */
    public void define(String name, String type, kind kind) throws Exception {
        switch(kind){
            case STATIC:
            case FIELD:
                typeTable.put(name, type);
                kindTable.put(name, kind);
                Integer index = indexTable.get(kind);
                indexOfTable.put(name, index);
                indexTable.put(kind, index + 1);
                break;
            case ARG:
            case VAR:
                subTypeTable.put(name, type);
                subKindTable.put(name, kind);
                Integer subIndex = indexTable.get(kind);
                subIndexOfTable.put(name, subIndex);
                indexTable.put(kind, subIndex + 1);
                break;
            case NONE:
                throw new Exception("Come on man, you can't have a none type");
        }
    }

    /**
     * Return the number of variables of the given kind already defined in the current scope
     * @param kind STATIC, FIELD, ARG, OR VAR
     * @return the number of variables already defined
     */
    public int varCount(kind kind){
        return indexTable.get(kind);
    }

    public kind KindOf(String name){
        return getFromHashtable(kindTable, subKindTable,name, kind.NONE);
    }

   public String typeOf(String name) throws Exception {
       return getFromHashtable(typeTable, subTypeTable,name);
   }

   public int indexOf(String name) throws Exception {
        return getFromHashtable(indexOfTable, subIndexOfTable,name);
   }


   private <R> R getFromHashtable(Hashtable<String, R> table, Hashtable<String, R> altTable, String name, R defaultReturn){
         if (table.containsKey(name)){
             return table.get(name);
         }
         else if (altTable.containsKey(name)){
             return altTable.get(name);
         }
         else{
             return defaultReturn;
         }
     }
     private <R> R getFromHashtable(Hashtable<String, R> table, Hashtable<String, R> altTable, String name) throws Exception {
        if (table.containsKey(name)){
            return table.get(name);
        }
        else if (altTable.containsKey(name)){
            return altTable.get(name);
        }
        else{
            throw new Exception("I don't know what to do with this, i dont have a reutrn");
        }
    }

}
