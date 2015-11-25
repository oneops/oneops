package com.oneops.transistor.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FlexManager {
	public long processFlex(long flexRelId, int step, boolean scaleUp, long envId);
}
