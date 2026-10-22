商品支付链路的主要逻辑图



<img width="770" height="571" alt="image" src="https://github.com/user-attachments/assets/1600d961-091b-40d0-8327-98f24b3969a5" />




下面是100qps测试

对于普通的查询



<img width="806" height="52" alt="image" src="https://github.com/user-attachments/assets/991d50df-d959-4be5-8586-f7dec8c00305" />



这是分类下的查询(加入了状态和分类的联合索引)



<img width="820" height="105" alt="image" src="https://github.com/user-attachments/assets/6e07df9d-d715-43ed-809d-64f2465ad15e" />





这是redis下的热榜查询,但是即便是有redis也还是很慢，所以说redis提速也不是什么时候都很明显的，也得考虑第一次



<img width="807" height="44" alt="image" src="https://github.com/user-attachments/assets/baa3c39b-a63c-4e69-b453-e114fa789100" />


所以就考虑加了个状态和销量的联合索引提速了


<img width="800" height="55" alt="image" src="https://github.com/user-attachments/assets/c2651bdc-126f-47f4-ab51-f8cb934d227c" />


接下来就测试10000并发度了

