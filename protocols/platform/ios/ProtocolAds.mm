/****************************************************************************
 Copyright (c) 2012+2013 cocos2d+x.org
 
 http://www.cocos2d+x.org
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

#include "ProtocolAds.h"
#include "PluginUtilsIOS.h"
#import "InterfaceAds.h"

namespace cocos2d { namespace plugin {

ProtocolAds::ProtocolAds()
: _listener(NULL)
{
}

ProtocolAds::~ProtocolAds()
{
}

void ProtocolAds::configDeveloperInfo(TAdsDeveloperInfo devInfo)
{
    if (devInfo.empty())
    {
        PluginUtilsIOS::outputLog("The developer info is empty for %s!", this->getPluginName());
        return;
    }
    else
    {
        PluginOCData* pData = PluginUtilsIOS::getPluginOCData(this);
        assert(pData != NULL);
        
        id ocObj = pData->obj;
        if ([ocObj conformsToProtocol:@protocol(InterfaceAds)]) {
            NSObject<InterfaceAds>* curObj = ocObj;
            NSMutableDictionary* dict = PluginUtilsIOS::createDictFromMap(&devInfo);
            [curObj configDeveloperInfo:dict];
        }
    }
}

void ProtocolAds::fetchAds(TAdsInfo info, AdsPos pos)
{
    PluginOCData* pData = PluginUtilsIOS::getPluginOCData(this);
    assert(pData != NULL);
    
    id ocObj = pData->obj;
    if ([ocObj conformsToProtocol:@protocol(InterfaceAds)]) {
        NSObject<InterfaceAds>* curObj = ocObj;
        NSMutableDictionary* dict = PluginUtilsIOS::createDictFromMap(&info);
        [curObj fetchAds:dict position:pos];
    }
}

void ProtocolAds::showAds(TAdsInfo info, AdsPos pos)
{
    PluginOCData* pData = PluginUtilsIOS::getPluginOCData(this);
    assert(pData != NULL);

    id ocObj = pData->obj;
    if ([ocObj conformsToProtocol:@protocol(InterfaceAds)]) {
        NSObject<InterfaceAds>* curObj = ocObj;
        NSMutableDictionary* dict = PluginUtilsIOS::createDictFromMap(&info);
        [curObj showAds:dict position:pos];
    }
}

void ProtocolAds::hideAds(TAdsInfo info)
{
    PluginOCData* pData = PluginUtilsIOS::getPluginOCData(this);
    assert(pData != NULL);
    
    id ocObj = pData->obj;
    if ([ocObj conformsToProtocol:@protocol(InterfaceAds)]) {
        NSObject<InterfaceAds>* curObj = ocObj;
        NSMutableDictionary* dict = PluginUtilsIOS::createDictFromMap(&info);
        [curObj hideAds:dict];
    }
}

void ProtocolAds::queryPoints()
{
    PluginUtilsIOS::callOCFunctionWithName(this, "queryPoints");
}

void ProtocolAds::spendPoints(int points)
{
    PluginOCData* pData = PluginUtilsIOS::getPluginOCData(this);
    assert(pData != NULL);
    
    id ocObj = pData->obj;
    if ([ocObj conformsToProtocol:@protocol(InterfaceAds)]) {
        NSObject<InterfaceAds>* curObj = ocObj;
        [curObj spendPoints:points];
    }
}

}} //namespace cocos2d { namespace plugin {
