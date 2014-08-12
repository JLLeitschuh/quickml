package quickml.supervised.classifier.splitOnAttributePredictiveModel;


import org.testng.Assert;
import org.testng.annotations.Test;
import quickml.data.Instance;
import quickml.supervised.PredictiveModelBuilder;
import quickml.supervised.PredictiveModelWithDataBuilder;
import quickml.supervised.classifier.TreeBuilderTestUtils;
import quickml.supervised.classifier.decisionTree.Tree;
import quickml.supervised.classifier.decisionTree.TreeBuilder;
import quickml.supervised.classifier.decisionTree.scorers.SplitDiffScorer;
import quickml.supervised.classifier.randomForest.RandomForest;
import quickml.supervised.classifier.randomForest.RandomForestBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Chris on 5/14/2014.
 */
public class SplitOnAttributePMBuilderTest {
    @Test
    public void simpleBmiTest() throws Exception {
        Set<String> whiteList = new HashSet<>();
        whiteList.add("weight");
        whiteList.add("height");
        final List<Instance<Map<String,Serializable>>> instances = TreeBuilderTestUtils.getInstances(10000);
        final TreeBuilder tb = new TreeBuilder(new SplitDiffScorer()).splitPredictiveModel("gender", whiteList);
        final RandomForestBuilder rfb = new RandomForestBuilder(tb);
        final SplitOnAttributePMBuilder cpmb = new SplitOnAttributePMBuilder("gender", (PredictiveModelBuilder)rfb, 10, 0.1, whiteList, 1);
        final long startTime = System.currentTimeMillis();
        final SplitOnAttributePM splitOnAttributePM = cpmb.buildPredictiveModel(instances);
        final RandomForest randomForest = (RandomForest) splitOnAttributePM.getDefaultPM();

        TreeBuilderTestUtils.serializeDeserialize(randomForest);
        final List<Tree> trees = randomForest.trees;
        final int treeSize = trees.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");
    }

    @Test
    public void simpleBmiTestSplit() throws Exception {
        final List<Instance<Map<String,Serializable>>> instances = TreeBuilderTestUtils.getInstances(1000);
        final PredictiveModelWithDataBuilder<Map<String,Serializable>,SplitOnAttributePM > wb = getWrappedUpdatablePredictiveModelBuilder();
        wb.splitNodeThreshold(1);
        final long startTime = System.currentTimeMillis();
        final SplitOnAttributePM  splitOnAttributePM = wb.buildPredictiveModel(instances);
        final RandomForest randomForest = (RandomForest) splitOnAttributePM.getDefaultPM();

        TreeBuilderTestUtils.serializeDeserialize(randomForest);

        final List<Tree> trees = randomForest.trees;
        final int treeSize = trees.size();
        final int firstTreeNodeSize = trees.get(0).node.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");

        final List<Instance<Map<String,Serializable>>> newInstances = TreeBuilderTestUtils.getInstances(1000);
        final SplitOnAttributePM  splitOnAttributePM1 = wb.buildPredictiveModel(newInstances);
        final RandomForest newRandomForest = (RandomForest) splitOnAttributePM1.getDefaultPM();
        Assert.assertTrue(splitOnAttributePM == splitOnAttributePM1, "Expect same tree to be updated");
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same number of trees");
        Assert.assertNotEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected new nodes");
    }

    @Test
    public void simpleBmiTestNoSplit() throws Exception {
        final List<Instance<Map<String,Serializable>>> instances = TreeBuilderTestUtils.getInstances(1000);
        final PredictiveModelWithDataBuilder<Map<String,Serializable>,SplitOnAttributePM > wb = getWrappedUpdatablePredictiveModelBuilder();
        final long startTime = System.currentTimeMillis();
        final SplitOnAttributePM  splitOnAttributePM = wb.buildPredictiveModel(instances);
        final RandomForest randomForest = (RandomForest) splitOnAttributePM.getDefaultPM();

        TreeBuilderTestUtils.serializeDeserialize(randomForest);

        final List<Tree> trees = randomForest.trees;
        final int treeSize = trees.size();
        final int firstTreeNodeSize = trees.get(0).node.size();
        Assert.assertTrue(treeSize < 400, "Forest size should be less than 400");
        Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000,"Building this node should take far less than 20 seconds");

        final List<Instance<Map<String,Serializable>>> newInstances = TreeBuilderTestUtils.getInstances(1000);
        final SplitOnAttributePM splitOnAttributePM1 = wb.buildPredictiveModel(newInstances);
        final RandomForest newRandomForest = (RandomForest) splitOnAttributePM1.getDefaultPM();
        Assert.assertTrue(splitOnAttributePM == splitOnAttributePM1, "Expect same tree to be updated");
        Assert.assertEquals(treeSize, newRandomForest.trees.size(), "Expected same number of trees");
        Assert.assertEquals(firstTreeNodeSize, newRandomForest.trees.get(0).node.size(), "Expected same nodes");
    }

    private PredictiveModelWithDataBuilder<Map<String,Serializable>,SplitOnAttributePM > getWrappedUpdatablePredictiveModelBuilder() {
        Set<String> whiteList = new HashSet<>();
        whiteList.add("weight");
        whiteList.add("height");
        final TreeBuilder tb = new TreeBuilder(new SplitDiffScorer()).splitPredictiveModel("gender", whiteList);
        final RandomForestBuilder urfb = new RandomForestBuilder(tb);
        final SplitOnAttributePMBuilder ucpmb = new SplitOnAttributePMBuilder("gender", (PredictiveModelBuilder)urfb, 10, 0.1, whiteList, 1);
        return new PredictiveModelWithDataBuilder<>(ucpmb);
    }
}
