package airldm2.database.rdf;

import org.openrdf.model.URI;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;


public class HistogramAggregationQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String INSTANCE_FILTER = "%instance_filter%";
   private static final String TARGET_GRAPH = "%target_graph%";
  
   private static final String QUERY = 
      "SELECT " + VALUE_VAR_PATTERN + " COUNT(" + VALUE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_FILTER + " "
      + TARGET_GRAPH
      + " } GROUP BY " + VALUE_VAR_PATTERN;
   
   private URI mInstance;
   private RbcAttribute mAttribute;
   
   public HistogramAggregationQueryConstructor(RDFDataDescriptor desc, String context, URI instance, RbcAttribute attribute) {
      super(desc, context);
      mInstance = instance;
      mAttribute = attribute;
   }

   public String createQuery() {
      String query;
      
      query = QUERY
         .replace(CONTEXT_PATTERN, mContextPart)
         .replace(VALUE_VAR_PATTERN, mAttribute.getGraphPattern().getValueVar())
         .replace(INSTANCE_FILTER, createInstanceFilter(mInstance))
         .replace(TARGET_GRAPH, createAttributeGraph(mAttribute));
      
      return query;
   }
   
}
