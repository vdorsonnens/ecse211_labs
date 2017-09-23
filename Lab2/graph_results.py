import matplotlib.pyplot as plt
import csv
import sys

def from_file(fname):
    X = []
    Y = []
    with open(fname) as fh:
        reader = csv.reader(fh)
        for row in reader:
            X.append(row[0])
            Y.append(row[1])
    return X, Y
    

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print('Need a filename')
        sys.exit(1)

    fname = sys.argv[1]
    X, Y = from_file(fname)

    # apply convulution to Y
    conv_Y = convolution(Y)

    # graph Y and conv_Y
    fig, ax = plt.scatter(X, Y)

     
