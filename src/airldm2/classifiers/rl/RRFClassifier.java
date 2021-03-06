package airldm2.classifiers.rl;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import explore.RDFDataDescriptorEnhancer;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.Timer;


public class RRFClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.RRFClassifier");
   static { Log.setLevel(Level.WARNING); }
      
   private LDInstances mInstances;
   private RDFDataDescriptor mDataDesc;
   private RDFDataSource mDataSource;

   private ClassEstimator mClassEst;
   
   private List<RDTClassifier> mForest;

   private final int mForestSize;
   private final int mAttributeSampleSize;
   private final int mDepthLimit;
   private Random mRandom;
   
   public RRFClassifier(int forestSize, int attributeSampleSize, int depthLimit) {
      mForestSize = forestSize;
      mAttributeSampleSize = attributeSampleSize;
      mDepthLimit = depthLimit;
      mRandom = new Random(0);
      mForest = CollectionUtil.makeList();
   }
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      Timer.INSTANCE.start("RRF learning");
      
      mInstances = instances;
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      
      new RDFDataDescriptorEnhancer(mDataSource).fillDomain(mDataDesc);
      
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      for (int i = 0; mForest.size() < mForestSize; i++) {
         Log.warning("Building tree " + i + " ... ");
         RDTClassifier tree = buildRDT();
         mForest.add(tree);
      }
      
      Log.warning("Final forest: " + mForest);
      
      Timer.INSTANCE.stop("RRF learning");
   }
   
   private RDTClassifier buildRDT() throws Exception {
      List<RbcAttribute> atts = CollectionUtil.makeList();
      for (RbcAttribute att : mDataDesc.getNonTargetAttributeList()) {
         atts.add(discretizeNonTargetAttribute(att));
      }
      
      List<RbcAttributeValue> allAttValues = RDTClassifier.propositionalizeAttributes(atts);
      
      Set<RbcAttributeValue> attValues = CollectionUtil.makeSet();
      while (attValues.size() < mAttributeSampleSize) {
         attValues.add(allAttValues.get(mRandom.nextInt(allAttValues.size())));
      }
      
      RDTClassifier rdt = new RDTClassifier(mDepthLimit);
      rdt.buildClassifier(mInstances, CollectionUtil.makeList(attValues));
      return rdt;
   }
   
   private RbcAttribute discretizeNonTargetAttribute(RbcAttribute att) throws RDFDatabaseException {
      AttributeEstimator estimator = att.getEstimator();
      if (estimator instanceof GaussianEstimator) {
         estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
         estimator.estimateParameters();
         
         Log.warning(estimator.toString());
         RbcAttribute discretizedAtt = ((GaussianEstimator) estimator).makeBinaryBinnedAttribute();
         return discretizedAtt;
      } else {
         return att;
      }
   }
   
   public List<RDTClassifier> getForest() {
      return mForest;
   }
   
   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      return null;
   }

}
