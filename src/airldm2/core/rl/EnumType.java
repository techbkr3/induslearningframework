package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.util.CollectionUtil;
import airldm2.util.StringUtil;

public class EnumType implements DiscreteType {

   public static String NAME = "ENUM";
   
   protected List<URI> mDomain;
   
   public EnumType(List<URI> domain) {
      mDomain = domain;
   }

   @Override
   public int domainSize() {
      return mDomain.size();
   }

   @Override
   public int indexOf(Value value) {
      if (value instanceof URI) {
         URI uri = (URI) value;
         return mDomain.indexOf(uri);
      }
      
      return -1;
   }

   @Override
   public String makeFilter(String varName, int valueIndex) {
      return makeFilter(varName, mDomain.get(valueIndex).toString());
   }

   @Override
   public String makeFilter(String varName, String value) {
      return new StringBuilder()
         .append("FILTER(")
         .append(varName)
         .append(" = <")
         .append(value)
         .append(">")
         .append(") ")
         .toString();
   }

   @Override
   public List<String> getStringValues() {
      return CollectionUtil.toStringList(mDomain);
   }

   @Override
   public String toString() {
      return NAME + "=" + StringUtil.toCSV(getStringValues());
   }
   
   @Override
   public void write(Writer out) throws IOException {
      out.write(NAME);
      out.write("=");
      out.write(StringUtil.toCSV(getStringValues()));
   }

}
