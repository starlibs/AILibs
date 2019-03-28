package hasco.metamining;

import org.junit.Test;

import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MetaMinerBasedSorterTest {

	@Test
	public void testCompareNodesSmaller() {
		// Given
		IMetaMiner mockedMetaMiner = mock(IMetaMiner.class);
		TFDNode mockedNode1 = mock(TFDNode.class);
		TFDNode mockedNode2 = mock(TFDNode.class);
		ComponentInstance cI1 = mock(ComponentInstance.class);
		ComponentInstance cI2 = mock(ComponentInstance.class);

		MetaMinerBasedSorter sorter = new MetaMinerBasedSorter(mockedMetaMiner, null);
		MetaMinerBasedSorter spy = spy(sorter);

		doReturn(cI1).when(spy).convertToComponentInstance(mockedNode1);
		doReturn(cI2).when(spy).convertToComponentInstance(mockedNode2);

		when(mockedMetaMiner.score(cI1)).thenReturn(1.0);
		when(mockedMetaMiner.score(cI2)).thenReturn(2.0);

		// When
		int comparison = spy.compare(mockedNode1, mockedNode2);

		// Then
		assertTrue(comparison < 0);
	}

	@Test
	public void testCompareNodesEquals() {
		// Given
		IMetaMiner mockedMetaMiner = mock(IMetaMiner.class);
		TFDNode mockedNode1 = mock(TFDNode.class);
		ComponentInstance cI1 = mock(ComponentInstance.class);

		MetaMinerBasedSorter sorter = new MetaMinerBasedSorter(mockedMetaMiner, null);
		MetaMinerBasedSorter spy = spy(sorter);

		doReturn(cI1).when(spy).convertToComponentInstance(mockedNode1);

		when(mockedMetaMiner.score(cI1)).thenReturn(1.0);

		// When
		int comparison = spy.compare(mockedNode1, mockedNode1);

		// Then
		assertTrue(comparison == 0);
	}

	public void testCompareNodesBigger() {
		// Given
		IMetaMiner mockedMetaMiner = mock(IMetaMiner.class);
		TFDNode mockedNode1 = mock(TFDNode.class);
		TFDNode mockedNode2 = mock(TFDNode.class);
		ComponentInstance cI1 = mock(ComponentInstance.class);
		ComponentInstance cI2 = mock(ComponentInstance.class);

		MetaMinerBasedSorter sorter = new MetaMinerBasedSorter(mockedMetaMiner, null);
		MetaMinerBasedSorter spy = spy(sorter);

		doReturn(cI1).when(spy).convertToComponentInstance(mockedNode1);
		doReturn(cI2).when(spy).convertToComponentInstance(mockedNode2);

		when(mockedMetaMiner.score(cI1)).thenReturn(2.0);
		when(mockedMetaMiner.score(cI2)).thenReturn(1.0);

		// When
		int comparison = spy.compare(mockedNode1, mockedNode2);

		// Then
		assertTrue(comparison > 0);
	}
}
