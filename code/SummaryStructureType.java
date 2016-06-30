public enum SummaryStructureType{ 
	DLeft, 
	BasicHeuristic, 
	EvictionWithCount, 
	EvictionWithoutCount, 
	MinReplacementHeuristic, 
	RollingMinWithBloomFilter, 
	RollingMinWihoutCoalescense, 
	RollingMinSingleLookup, 
	OverallMinReplacement,
	CountMinCacheNoKeys,
	CountMinCacheNoKeysReportedBit,
	CountMinCacheWithKeys,
	GroupCounters,
	AsymmetricDleftSingleLookUp
};