CC = arm-linux-androideabi-gcc

CFLAGS += -Os -s
CFLAGS += -llog
#CFLAGS += -Wall -pedantic

su : su.c arg.h
	$(CC) $(CFLAGS) su.c -o su

clean :
	-rm su
