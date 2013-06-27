/*
 //  su.c
 //
 //
 //  Created by koneu on 14.03.13.
 //
 */
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>

#include <android/log.h>
#define log(...) __android_log_print(ANDROID_LOG_INFO, "µSU", __VA_ARGS__)

#include "arg.h"
char *argv0;

static int check_perm(const uid_t user_old);
static void usage(void);

static int check_perm(const uid_t user_old) {
	int ret = 0;
	if(user_old) {
		FILE *f;
		f = fopen("/data/data/koneu.usu/files/permissions", "r");
		if(f) {
			int line;
			while(!feof(f) && (fscanf(f, "%i", &line) != EOF)) {
				if(line == user_old) {
					ret = 1;
				}
			}
			fclose(f);
		}
	} else {
		ret = 1;
	}
	switch(ret) {
		case 1:
			log("granting permission to UID %i", user_old);
			break;
		case 0:
			log("denying permission to UID %i", user_old);
			break;
	}
	return ret;
}


static void usage(void) {
	printf("usage: %s [-lfmpv] [-s shell] [-c command] [USER]\n", argv0);
	exit(EXIT_FAILURE);
}

int main(int argc, char *argv[]) {
	int user_new = 0;
	char *command = NULL, *shell = getenv("SHELL");
	ARGBEGIN {
	case 'l':
	case 'f':
	case 'm':
	case 'p':
		break;
	case 's':
		shell = EARGF(usage());
		break;
    case 'c':
		command = EARGF(usage());
		break;
	case 'v':
		puts("µSU v0.45");
	default:
		usage();
		break;
	} ARGEND;
	if(argc > 0) {
		user_new = atoi(argv[0]);
		argc--;
	}
	if(argc > 0) {
		usage();
	}
	if(check_perm(getuid())) {
		if(!shell) {
			shell = "sh";
		}
		if(setuid(user_new)) {
			log("setuid(%i) failed", user_new);
			exit(EXIT_FAILURE);
		}
		setgid(user_new);
		if(command) {
			execlp(shell, "sh", "-c", command, NULL);
		}
		execlp(shell, "sh", "-", NULL);
	}
	exit(EXIT_FAILURE);
}
