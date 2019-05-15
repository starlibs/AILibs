# Readme ExtendedSingleEnhancer and MetaBuildingBlock System

## ExtendedSingleEnhancer

--* All ExtendedSingleEnhancer inherit from MultiClassifersCombiner and thus support -B to add base classifiers
--* It is recommended to always call setOptions() before buildClassifer(), because there are no useful default options
--* It is recommended to always set the Flag -B when using setOptions(), because otherwise there are not multiple classifiers to combine

## MetaBuildingBlock System

### How does the MetaBuildingBlock System works?

There are two types of MetaBuildingBlocks: InnerMetaBuildingBlocks and EndingMetaBuildingBlocks. InnerMetaBuildingBlocks require a base classifier and
another MetaBuildingBlock. EndingMetaBuildingBlocks only require a base classifier. Thus with this system it is possible to define an arbitrary number
of base classifier for every meta classifier.

### Nice to notice

Note that in ML-Plan it is very likely, that every meta classifier starts with two base classifiers and adds from time to time more and more base classifiers,
if they ensure a significant boost of performance. We approve this behaviour and describe it as a natural involvement of the meta classifier