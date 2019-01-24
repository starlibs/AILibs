% Loads Kebi Label Ranking datasets which are provided in XXL format.
% XXL is a Java library created by the Seeger group from the university of
% Marburg.
%
% Input:
%      filename - a string denoting the filename
%
% Output:
%      features - an Nxp matrix of instance features (row wise)
%      rankings - an NxM matrix of rankings (row wise), NOT orderings
%
% Example: [features, rankings] = load_lr_xxl('data/iris_dense.txt')
% 2014-12, disc
function [features, rankings] = load_lr_xxl(filename)
    impdata=importdata(filename);
    hstr = impdata.textdata{1};
    posL = findstr(hstr,'L');
    numLabels = str2num(hstr(posL(end)+1:length(hstr)));  
    posF = findstr(hstr,'A');
    dimFeatures = str2num(hstr(posF(end)+1:posL(1)-1));
    features = impdata.data(:,1:dimFeatures);
    rankings = impdata.data(:,dimFeatures+1:dimFeatures+numLabels);
end