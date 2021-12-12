#include <stdio.h>

int main(){
    char in[255];
    char pwd[] = "safeheslo\0";
        
    while (1) {
        printf("Zadaj heslo");
        gets(input);
	// fgets(in, 20, stdin);
	printf("skusam heslo: ");
	// printf(in);
	printf("%s", in);
        if (strcmp(&in, pwd) == 0) {
            printf("spravne heslo!");
        } else {
            printf("nespravne heslo");
        }
        return 0;
    }
} 