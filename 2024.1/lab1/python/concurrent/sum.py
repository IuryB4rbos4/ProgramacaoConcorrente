import os, os.path
import threading
import sys
mutex = threading.Semaphore(1)

def sum_thread(thread_idx, size, res):
    for i in range(size[0], size[1]):
        mutex.acquire()
        res[thread_idx] += do_sum("../../dataset/" + "file." + str(i  + 1))
        mutex.release()

def do_sum(path):
    _sum = 0
    with open(path, 'rb',buffering=0) as f:
        byte = f.read(1)
        while byte:
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        return _sum

def count_files():
    return len(os.listdir('../../dataset/'))

if __name__ == "__main__":
    num_threads = 4
    res = [0] * num_threads
    threads = []
    count = count_files() // num_threads
    for i in range(num_threads):
        t = threading.Thread(target =sum_thread, args=(i, (i * count,
            i * count + count), res))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    print(res)

    print(sum(res))