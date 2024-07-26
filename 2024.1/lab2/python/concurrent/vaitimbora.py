import os, os.path
import threading
import sys

mutex = threading.Semaphore(1)
paths = os.listdir(sys.argv[1])
semaphore = threading.Semaphore(len(paths) // 2)
res = 0
dict = {}

def sum_thread(i):
    global res, dict
    mutex.acquire()
    sum = do_sum(sys.argv[1] + "file." + str(i  + 1))
    res += sum
    if sum in dict:
        dict[sum].append("file." + str(i  + 1))
    else:
        dict[sum] = ["file." + str(i  + 1)]
    mutex.release()


def do_sum(path):
    _sum = 0
    with open(path, 'rb',buffering=0) as f:
        byte = f.read(1)
        semaphore.acquire()
        while byte:
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        semaphore.release()    
        return _sum



if __name__ == "__main__":
    threads = []
    for i in range(len(paths)):
        t = threading.Thread(target =sum_thread, args=(i,))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    for soma in dict:
        if (len(dict[soma]) > 1):
            print(str(soma) + " " + " ".join(dict[soma]))