%% Reads the TSPLib95 format (with euclidean coordinates)
% see http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/
% The method returns a 2xN matrix of (city) coordinates.
% Disc, 2015-01-21
function [cities]=read_tsplib95(filename)
    f = fopen(filename);
    cnt = 1;
    while 1
        l = fgetl(f);
        if (strcmp(l,'EOF')==1) 
            break; 
        elseif (l==-1)
            break;
        end
        [data,  count, errmsg, nextindex] = sscanf(l,'%d %f %f',3);
        if (count==3)
            temp_cities(cnt,:) = data(2:3)';
            cnt = cnt +1;
        end
       
    end
    fclose(f); 
    cities = temp_cities';