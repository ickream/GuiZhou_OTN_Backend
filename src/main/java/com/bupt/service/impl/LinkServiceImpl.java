package com.bupt.service.impl;

import com.bupt.dao.ResLinkDao;
import com.bupt.entity.ResLink;
import com.bupt.pojo.LinkCreateInfo;
import com.bupt.pojo.LinkDTO;
import com.bupt.service.LinkService;
import com.bupt.service.NetElementService;
import com.bupt.util.exception.controller.result.NoneGetException;
import com.bupt.util.exception.controller.result.NoneRemoveException;
import com.bupt.util.exception.controller.result.NoneSaveException;
import com.bupt.util.exception.controller.result.NoneUpdateException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("linkService")
public class LinkServiceImpl implements LinkService {
    @Resource
    private ResLinkDao resLinkDao;
    @Resource
    private NetElementService netElementService;

    @Override
    public LinkDTO saveResLink(Long versionId, LinkCreateInfo linkCreateInfo) {
        ResLink insertInfo = convertToResLink(linkCreateInfo);
        insertInfo.setVersionId(versionId);
        if (resLinkDao.insertSelective(insertInfo) > 0) {
            return convertToResLinkDTO(resLinkDao.selectOne(insertInfo));
        }
        throw new NoneSaveException();
    }

    @Override
    @Transactional
    public void listRemoveResLink(Long versionId, List<Long> linkIdList) {
        for (Long aLinkIdList : linkIdList) {
            if (resLinkDao.deleteByPrimaryKey(aLinkIdList) == 0) {
                throw new NoneRemoveException();
            }
        }
    }

    @Override
    public LinkDTO updateResLink(Long versionId, Long linkId, LinkCreateInfo linkCreateInfo) {
        ResLink updateInfo = convertToResLink(linkCreateInfo);
        if (resLinkDao.updateByExampleSelective(convertToResLink(linkCreateInfo), getExample(versionId, linkId)) == 1) {
            return convertToResLinkDTO(resLinkDao.selectOne(updateInfo));
        }
        throw new NoneUpdateException();
    }

    private Example getExample(Long versionId, Long linkId) {
        Example updateExample = new Example(ResLink.class);
        Example.Criteria criteria = updateExample.createCriteria();
        criteria.andEqualTo("versionId", versionId);
        criteria.andEqualTo("linkId", linkId);
        return updateExample;
    }


    private Example getExample(Long versionId) {
        Example example = new Example(ResLink.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("versionId", versionId);
        return example;
    }


    @Override
    public List<LinkDTO> getResLink(Long versionId) {
        List<ResLink> resLinksList = resLinkDao.selectByExample(getExample(versionId));
        if (resLinksList.size() == 0) {
            throw new NoneGetException();
        }
        List<LinkDTO> linkDTOList = new ArrayList<>();
        for (ResLink aResLinksList : resLinksList) {
            linkDTOList.add(this.convertToResLinkDTO(aResLinksList));
        }
        return linkDTOList;
    }

    @Override
    public void batchRemove(Long versionId) {
        resLinkDao.deleteByExample(getExample(versionId));
    }

    @Override
    public void batchCreate(Long baseVersionId, Long newVersionId) {
        //TODO 链路中的网元ID需要更新
        List<ResLink> resLinksList = resLinkDao.selectByExample(getExample(baseVersionId));
        for (ResLink link : resLinksList) {
            LinkCreateInfo newLink = new LinkCreateInfo();
            BeanUtils.copyProperties(link, newLink);
            newLink.setEndAId(netElementService.getNewElementId(baseVersionId, newLink.getEndAId(), newVersionId));
            newLink.setEndZId(netElementService.getNewElementId(baseVersionId, newLink.getEndZId(), newVersionId));
            saveResLink(newVersionId, newLink);
        }
    }


    private ResLink convertToResLink(Object inputObject) {
        if (null == inputObject) {
            return null;
        }
        ResLink resLink = new ResLink();
        BeanUtils.copyProperties(inputObject, resLink);
        return resLink;
    }

    private LinkDTO convertToResLinkDTO(Object inputObject) {
        if (null == inputObject) {
            return null;
        }
        LinkDTO linkDTO = new LinkDTO();
        BeanUtils.copyProperties(inputObject, linkDTO);
        return linkDTO;
    }

}
