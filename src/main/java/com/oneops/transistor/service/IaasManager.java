package com.oneops.transistor.service;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.transistor.domain.IaasRequest;

@Transactional
public interface IaasManager {
	public long processPlatformIaas(IaasRequest request, long platformId, String userId);
}
