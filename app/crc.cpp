#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
using namespace std;

#define                 P_KERMIT    0x8408
#define TRUE (1==1)
#define FALSE (!TRUE)
static int              crc_tabkermit_init      = FALSE;
static unsigned short   crc_tabkermit[256];


static void init_crckermit_tab( void ) {

    int i, j;
    unsigned short crc, c;

    for (i=0; i<256; i++) {

        crc = 0;
        c   = (unsigned short) i;
        for (j=0; j<8; j++) {
            //printf("%d %x\n", j, (crc ^ c) & 0x0001);
//            printf("%d %x %x\n", j, (crc >> 1) ^ P_KERMIT, crc >> 1);
            if ( (crc ^ c) & 0x0001 )
                crc = ( crc >> 1 ) ^ P_KERMIT;
            else
                crc =   crc >> 1;

            c = c >> 1;
        }

        crc_tabkermit[i] = crc;
//        printf(" %x", crc);
    }

    printf("\n");
    crc_tabkermit_init = TRUE;

}  /* init_crckermit_tab */


unsigned short update_crc_kermit( unsigned short crc, char c ) {

    unsigned short tmp, short_c;

    short_c = 0x00ff & (unsigned short) c;
//    printf("sc: %x\n", short_c);
    if ( ! crc_tabkermit_init ) init_crckermit_tab();

    tmp =  crc       ^ short_c;
    crc = (crc >> 8) ^ crc_tabkermit[ tmp & 0xff ];

    return crc;

}  /* update_crc_kermit */


uint16_t crc16_kermit(uint8_t *buf, int len)
{
  uint16_t crc_kermit=0;
  char *ptr = (char*)buf;
  int i;

  for(i=0; i<len; i++) {
	crc_kermit     = update_crc_kermit( crc_kermit, *ptr);
	ptr++;
  }
  return crc_kermit;
}
uint8_t atoh(uint8_t a) {
    if (a >= '0' && a <= '9')
        return a - '0';
    if (a >= 'a' && a <= 'f')
        return a - 'a' + 10;
    if (a >= 'A' && a <= 'F')
        return a - 'A' + 10;
    return 0;
}

int main(int argc, char** argv) {
    printf("%d, %s\n", argc, argv[1]);
    char* str = NULL;
    uint8_t *data = NULL;
    int len = 0;
    if (argc == 2) {
        str = argv[1];
        int size = strlen(str);
        len = (size + 1) / 2;
        data = new uint8_t[len];
        for (int i=0; i<size; i+=2) {
            uint8_t L, H;
            if (i+1 == size) {
                L = str[i];
                H = 0;
            } else {
                L = str[i+1];
                H = str[i];
            }
            L = atoh(L);
            H = atoh(H);
            data[i/2] = (H << 4) + L;
            printf("%x\n", data[i/2]);
        }
    } else {
        uint8_t buf[] = {0xaa, 0xaf, 0xfa, 0x01, 0x00, 0x01, 0x00, 0x00};
        len = sizeof(buf);
        data = buf;
    }
    printf("crc16: %x\n", crc16_kermit(data, len));
    return 0;
}
