with open("heatmap.css", "w" ) as file:
    file.write("edge { \n \t padding 100px; \n } \n\n")
    file.write("node { \n \t size-mode: fit; \n\t fill-mode: plain; \n\t fill-color: grey; \n\t padding: 5px; \n}\n\n")
    file.write("node.root {\n\t padding: 10px; \n\t fill-color:black; \n}\n\n")

    b = 255
    r = 0

    for i in range(511):
        print("%X, %X"%(b,r))
        file.write ("node.n%(i)s {\n\tfill-color: rgb(%(red)s,00,%(blue)s)}\n\n"%{"i": i, "red": r, "blue": b})
        if i < 255:
            r += 1
        else:
            b -=1
            

