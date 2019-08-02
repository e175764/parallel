import gc
import glob
import time
import cv2
from joblib import Parallel, delayed
import json
import my_pickle as mp

f = open('set.json', 'r', encoding="utf-8")
json_data = json.load(f)
#画像が保存されているルートディレクトリのパス(水増し前)
root_dir = json_data["images"]
save = json_data["save_npy"]
infx = json_data["infx"]
infy = json_data["infy"]
# 商品名
categories = ["映える","映えない"]
flag=True
X=[]
num=4
max=15000

if __name__ == '__main__':
		image_path_list = glob.glob(root_dir +'/*.jpg')[:max]
		
		print("Sequentially")
		t1=time.time()
		images = [cv2.imread(image_path) for image_path in image_path_list]
		t2=time.time()
		t_seq=t2-t1
		del images; gc.collect()
		
		print("MultiThreading")
		t1=time.time()
		images = Parallel(n_jobs=num, backend='threading')(\
		delayed(cv2.imread)(image_path) for image_path in image_path_list)
		t2=time.time()
		t_thre=t2-t1
		del images; gc.collect()
		
		print("MultiProcessing")
		t1=time.time()
		images = Parallel(n_jobs=num, backend='multiprocessing')(\
		delayed(cv2.imread)(image_path) for image_path in image_path_list)
		t2=time.time()
		t_pro=t2-t1
		print(max,t_seq,t_thre,t_pro)

#画像データごとにadd_sample()を呼び出し、X,Yの配列を返す関数


x = images
#データを保存する（データの名前を「tea_data.npy」としている）
mp.pickle_dump(x, save+"insta_data3.sav") 