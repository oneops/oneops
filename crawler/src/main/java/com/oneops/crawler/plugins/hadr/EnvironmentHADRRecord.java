package com.oneops.crawler.plugins.hadr;

import java.io.Serializable;

public class EnvironmentHADRRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	    private int total;

	    private String nsPath;

	    private String platform;

	    private String ctoOrg;

	    private String ctoDirect;

	    private String ooUrl;

	    private int envsInAssembly;

	    private String assembly;

	    private String sClouds;

	    private String createdTS;

	    private String env;

	    private String pack;

	    private String vp;

	    private String org;

	    private int packVersion;

	    private boolean isDR;

	    private String plat;

	    private String source;

	    private String[] secondaryClouds;

	    private String pClouds;

	    private String[] primaryClouds;

	    private String sourcePack;

	    private boolean isHA;

	    private CCount cCount;

	    public int getTotal ()
	    {
	        return total;
	    }

	    public void setTotal (int total)
	    {
	        this.total = total;
	    }

	    public String getNsPath ()
	    {
	        return nsPath;
	    }

	    public void setNsPath (String nsPath)
	    {
	        this.nsPath = nsPath;
	    }

	    public String getPlatform ()
	    {
	        return platform;
	    }

	    public void setPlatform (String platform)
	    {
	        this.platform = platform;
	    }

	    public String getCtoOrg ()
	    {
	        return ctoOrg;
	    }

	    public void setCtoOrg (String ctoOrg)
	    {
	        this.ctoOrg = ctoOrg;
	    }

	    public String getCtoDirect ()
	    {
	        return ctoDirect;
	    }

	    public void setCtoDirect (String ctoDirect)
	    {
	        this.ctoDirect = ctoDirect;
	    }

	    public String getOoUrl ()
	    {
	        return ooUrl;
	    }

	    public void setOoUrl (String ooUrl)
	    {
	        this.ooUrl = ooUrl;
	    }

	    public int getEnvsInAssembly ()
	    {
	        return envsInAssembly;
	    }

	    public void setEnvsInAssembly (int envsInAssembly)
	    {
	        this.envsInAssembly = envsInAssembly;
	    }

	    public String getAssembly ()
	    {
	        return assembly;
	    }

	    public void setAssembly (String assembly)
	    {
	        this.assembly = assembly;
	    }

	    public String getSClouds ()
	    {
	        return sClouds;
	    }

	    public void setSClouds (String sClouds)
	    {
	        this.sClouds = sClouds;
	    }

	    public String getCreatedTS ()
	    {
	        return createdTS;
	    }

	    public void setCreatedTS (String createdTS)
	    {
	        this.createdTS = createdTS;
	    }

	    public String getEnv ()
	    {
	        return env;
	    }

	    public void setEnv (String env)
	    {
	        this.env = env;
	    }

	    public String getPack ()
	    {
	        return pack;
	    }

	    public void setPack (String pack)
	    {
	        this.pack = pack;
	    }

	    public String getVp ()
	    {
	        return vp;
	    }

	    public void setVp (String vp)
	    {
	        this.vp = vp;
	    }

	    public String getOrg ()
	    {
	        return org;
	    }

	    public void setOrg (String org)
	    {
	        this.org = org;
	    }

	    public int getPackVersion ()
	    {
	        return packVersion;
	    }

	    public void setPackVersion (int packVersion)
	    {
	        this.packVersion = packVersion;
	    }

	    public boolean getIsDR ()
	    {
	        return isDR;
	    }

	    public void setIsDR (boolean isDR)
	    {
	        this.isDR = isDR;
	    }

	    public String getPlat ()
	    {
	        return plat;
	    }

	    public void setPlat (String plat)
	    {
	        this.plat = plat;
	    }

	    public String getSource ()
	    {
	        return source;
	    }

	    public void setSource (String source)
	    {
	        this.source = source;
	    }

	    public String[] getSecondaryClouds ()
	    {
	        return secondaryClouds;
	    }

	    public void setSecondaryClouds (String[] secondaryClouds)
	    {
	        this.secondaryClouds = secondaryClouds;
	    }

	    public String getPClouds ()
	    {
	        return pClouds;
	    }

	    public void setPClouds (String pClouds)
	    {
	        this.pClouds = pClouds;
	    }

	    public String[] getPrimaryClouds ()
	    {
	        return primaryClouds;
	    }

	    public void setPrimaryClouds (String[] primaryClouds)
	    {
	        this.primaryClouds = primaryClouds;
	    }

	    public String getSourcePack ()
	    {
	        return sourcePack;
	    }

	    public void setSourcePack (String sourcePack)
	    {
	        this.sourcePack = sourcePack;
	    }

	    public boolean getIsHA ()
	    {
	        return isHA;
	    }

	    public void setIsHA (boolean isHA)
	    {
	        this.isHA = isHA;
	    }

	    public CCount getCCount ()
	    {
	        return cCount;
	    }

	    public void setCCount (CCount cCount)
	    {
	        this.cCount = cCount;
	    }

	    @Override
	    public String toString()
	    {
	        return "EnvironmentHADRRecord [total = "+total+", nsPath = "+nsPath+", platform = "+platform+", ctoOrg = "+ctoOrg+", ctoDirect = "+ctoDirect+", ooUrl = "+ooUrl+", envsInAssembly = "+envsInAssembly+", assembly = "+assembly+", sClouds = "+sClouds+", createdTS = "+createdTS+", env = "+env+", pack = "+pack+", vp = "+vp+", org = "+org+", packVersion = "+packVersion+", isDR = "+isDR+", plat = "+plat+", source = "+source+", secondaryClouds = "+secondaryClouds+", pClouds = "+pClouds+", primaryClouds = "+primaryClouds+", sourcePack = "+sourcePack+", isHA = "+isHA+", cCount = "+cCount+"]";
	    }
	}
	
	

