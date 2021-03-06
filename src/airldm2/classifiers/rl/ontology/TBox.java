package airldm2.classifiers.rl.ontology;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class TBox {

   private SimpleDirectedGraph<URI,DefaultEdge> mSubclass;
   private SimpleDirectedGraph<URI,DefaultEdge> mSubclassClosed;
   
   public TBox() {
      mSubclass = new SimpleDirectedGraph<URI,DefaultEdge>(DefaultEdge.class);
   }
   
   public SimpleDirectedGraph<URI,DefaultEdge> getOriginal() {
      return mSubclass;
   }
   
   public SimpleDirectedGraph<URI,DefaultEdge> getClosed() {
      return mSubclassClosed;
   }
   
   public void addSubclass(URI sub, URI sup) {
      mSubclass.addVertex(sub);
      mSubclass.addVertex(sup);
      mSubclass.addEdge(sub, sup);
   }
   
   public void computeClosure() {
      mSubclassClosed = new SimpleDirectedGraph<URI,DefaultEdge>(DefaultEdge.class);
      Graphs.addGraph(mSubclassClosed, mSubclass);
      TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(mSubclassClosed);
   }
   
   public Cut getRootCut(URI root) {
      return new Cut(this, root);
   }
   
   public List<URI> getDirectSubclass(URI c) {
      if (!mSubclass.containsVertex(c)) return Collections.emptyList();
      
      return Graphs.predecessorListOf(mSubclass, c);
   }
   
   public List<URI> getAllSubclasses(URI c) {
      if (!mSubclass.containsVertex(c)) return Collections.emptyList();
      
      return Graphs.predecessorListOf(mSubclassClosed, c);
   }
   
   public URI getDirectSuperclass(URI c) {
      if (!mSubclass.containsVertex(c)) return null;
      
      List<URI> successors = Graphs.successorListOf(mSubclass, c);
      return successors.get(0);
   }
   
   public List<URI> getSiblings(URI c) {
      return getDirectSubclass(getDirectSuperclass(c));
   }
   
   public List<URI> getSuperclasses(URI c) {
      if (!mSubclassClosed.containsVertex(c)) return Collections.emptyList();
      
      return Graphs.successorListOf(mSubclassClosed, c);
   }

   public int getSuperclassSize(URI c) {
      return mSubclass.outDegreeOf(c);
   }
   
   public Cut getLeafCut(URI root) {
      List<URI> leaf = CollectionUtil.makeList();
      for (URI pred : Graphs.predecessorListOf(mSubclassClosed, root)) {
         if (mSubclassClosed.inDegreeOf(pred) == 0) {
            leaf.add(pred);
         }
      }
      return new Cut(this, leaf);
   }

   public Cut getAllNodesAsCut(URI root) {
      List<URI> all = CollectionUtil.makeList(mSubclass.vertexSet());
      return new Cut(this, all);
   }

   public Set<URI> getClasses() {
      return mSubclass.vertexSet();
   }
   
   public List<URI> getLeaves() {
      List<URI> leaves = CollectionUtil.makeList();
      for (URI uri : mSubclassClosed.vertexSet()) {
         if (mSubclassClosed.inDegreeOf(uri) == 0) {
            leaves.add(uri);
         }
      }
      return leaves;
   }

   public boolean isLeaf(URI c) {
      return mSubclassClosed.inDegreeOf(c) == 0;
   }

   
   private Map<URI, List<List<URI>>> mLayers = CollectionUtil.makeMap();
   
   public List<List<URI>> getLayers(URI root) {
      List<List<URI>> layers = mLayers.get(root);
      if (layers == null) {
         layers = CollectionUtil.makeList();
         mLayers.put(root, layers);
         
         layers.add(Arrays.asList(root));
         for (int i = 0; true; i++) {
            List<URI> lastLayer = layers.get(i);
            List<URI> nextLayer = CollectionUtil.makeList();
            for (URI uri : lastLayer) {
               List<URI> directSubclass = getDirectSubclass(uri);
               nextLayer.addAll(directSubclass);
            }
            
            if (nextLayer.isEmpty()) break;
            layers.add(nextLayer);
         }
      }
      return layers;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

}
