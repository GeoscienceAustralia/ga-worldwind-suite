package au.gov.ga.worldwind.viewer.panels.dataset;

import java.util.List;

import au.gov.ga.worldwind.common.ui.lazytree.ITreeObject;

public interface IDataset extends IData, ITreeObject
{
	List<IData> getChildren();
	void addChild(IData child);
}